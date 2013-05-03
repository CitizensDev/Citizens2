package net.citizensnpcs.npc;

import java.util.Set;

import javax.annotation.Nullable;

import net.citizensnpcs.NPCNeedsRespawnEvent;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_5_R3.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CitizensNPC extends AbstractNPC {
    private EntityController entityController;
    private final CitizensNavigator navigator = new CitizensNavigator(this);

    public CitizensNPC(int id, String name, EntityController entityController) {
        super(id, name);
        Preconditions.checkNotNull(entityController);
        this.entityController = entityController;
    }

    @Override
    public boolean despawn(DespawnReason reason) {
        if (!isSpawned()) {
            Messaging.debug("Tried to despawn", getId(), "while already despawned.");
            return false;
        }

        NPCDespawnEvent event = new NPCDespawnEvent(this, reason);
        if (reason == DespawnReason.CHUNK_UNLOAD)
            event.setCancelled(Setting.KEEP_CHUNKS_LOADED.asBoolean());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            getBukkitEntity().getLocation().getChunk().load();
            Messaging.debug("Couldn't despawn", getId(), "due to despawn event cancellation. Force loaded chunk.");
            return false;
        }
        boolean keepSelected = getTrait(Spawned.class).shouldSpawn();
        if (!keepSelected)
            data().remove("selectors");
        for (Trait trait : traits.values())
            trait.onDespawn();
        navigator.onDespawn();
        entityController.remove();
        return true;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        return entityController.getBukkitEntity();
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public boolean isSpawned() {
        return getBukkitEntity() != null;
    }

    @Override
    public void load(final DataKey root) {
        metadata.loadFrom(root.getRelative("metadata"));
        // Load traits

        String traitNames = root.getString("traitnames");
        Set<DataKey> keys = Sets.newHashSet(root.getRelative("traits").getSubKeys());
        Iterables.addAll(keys, Iterables.transform(Splitter.on(',').split(traitNames), new Function<String, DataKey>() {
            @Override
            public DataKey apply(@Nullable String input) {
                return root.getRelative("traits." + input);
            }
        }));
        for (DataKey traitKey : keys) {
            if (traitKey.keyExists("enabled") && !traitKey.getBoolean("enabled")
                    && traitKey.getRaw("enabled") instanceof Boolean) {
                // we want to avoid coercion here as YAML can coerce map
                // existence to boolean
                continue;
            }
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitKey.name());
            Trait trait;
            if (hasTrait(clazz)) {
                trait = getTrait(clazz);
            } else {
                trait = CitizensAPI.getTraitFactory().getTrait(clazz);
                if (trait == null) {
                    Messaging.severeTr(Messages.SKIPPING_BROKEN_TRAIT, traitKey.name(), getId());
                    continue;
                }
                addTrait(trait);
            }
            loadTrait(trait, traitKey);
        }

        // Spawn the NPC
        CurrentLocation spawnLocation = getTrait(CurrentLocation.class);
        if (getTrait(Spawned.class).shouldSpawn() && spawnLocation.getLocation() != null)
            spawn(spawnLocation.getLocation());

        navigator.load(root.getRelative("navigator"));
    }

    private void loadTrait(Trait trait, DataKey traitKey) {
        try {
            trait.load(traitKey);
            PersistenceLoader.load(trait, traitKey);
        } catch (Throwable ex) {
            Messaging.logTr(Messages.TRAIT_LOAD_FAILED, traitKey.name(), getId());
        }
    }

    @Override
    public void save(DataKey root) {
        super.save(root);
        navigator.save(root.getRelative("navigator"));
    }

    @Override
    public void setBukkitEntityType(EntityType type) {
        EntityController controller = EntityControllers.createForType(type);
        if (controller == null)
            throw new IllegalArgumentException("Unsupported entity type " + type);
        setEntityController(controller);
    }

    public void setEntityController(EntityController newController) {
        Preconditions.checkNotNull(newController);
        boolean wasSpawned = isSpawned();
        Location prev = null;
        if (wasSpawned) {
            prev = getBukkitEntity().getLocation();
            despawn(DespawnReason.PENDING_RESPAWN);
        }
        entityController = newController;
        if (wasSpawned)
            spawn(prev);
    }

    @Override
    public boolean spawn(Location at) {
        Preconditions.checkNotNull(at, "location cannot be null");
        if (isSpawned()) {
            Messaging.debug("Tried to spawn", getId(), "while already spawned.");
            return false;
        }

        entityController.spawn(at, this);
        EntityLiving mcEntity = ((CraftLivingEntity) getBukkitEntity()).getHandle();
        boolean couldSpawn = !Util.isLoaded(at) ? false : mcEntity.world.addEntity(mcEntity, SpawnReason.CUSTOM);
        if (!couldSpawn) {
            Messaging.debug("Retrying spawn of", getId(), "later due to chunk being unloaded.");
            // we need to wait for a chunk load before trying to spawn
            entityController.remove();
            Bukkit.getPluginManager().callEvent(new NPCNeedsRespawnEvent(this, at));
            return true;
        }

        NMS.setHeadYaw(mcEntity, at.getYaw());
        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, at);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled()) {
            entityController.remove();
            Messaging.debug("Couldn't spawn", getId(), "due to event cancellation.");
            return false;
        }

        getBukkitEntity().setMetadata(NPC_METADATA_MARKER, new FixedMetadataValue(CitizensAPI.getPlugin(), true));

        // Set the spawned state
        getTrait(CurrentLocation.class).setLocation(at);
        getTrait(Spawned.class).setSpawned(true);

        navigator.onSpawn();
        // Modify NPC using traits after the entity has been created
        for (Trait trait : traits.values())
            trait.onSpawn();
        getBukkitEntity().setRemoveWhenFarAway(false);
        getBukkitEntity().setCustomName(getFullName());
        return true;
    }

    @Override
    public void update() {
        try {
            super.update();
            if (isSpawned()) {
                NMS.trySwim(getBukkitEntity());
                navigator.run();
            }
        } catch (Exception ex) {
            Messaging.logTr(Messages.EXCEPTION_UPDATING_NPC, getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static final String NPC_METADATA_MARKER = "NPC";

}