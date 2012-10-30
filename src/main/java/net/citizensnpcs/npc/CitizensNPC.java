package net.citizensnpcs.npc;

import java.util.List;

import javax.annotation.Nullable;

import net.citizensnpcs.EventListen;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.EntityLiving;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class CitizensNPC extends AbstractNPC {
    protected EntityLiving mcEntity;
    private final CitizensNavigator navigator = new CitizensNavigator(this);
    private final List<String> removedTraits = Lists.newArrayList();

    protected CitizensNPC(int id, String name) {
        super(id, name);
    }

    protected abstract EntityLiving createHandle(Location loc);

    @Override
    public boolean despawn() {
        if (!isSpawned())
            return false;

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));
        boolean keepSelected = getTrait(Spawned.class).shouldSpawn();
        if (!keepSelected)
            data().remove("selectors");
        for (Trait trait : traits.values())
            trait.onDespawn();
        getBukkitEntity().remove();
        mcEntity = null;

        return true;
    }

    @Override
    public LivingEntity getBukkitEntity() {
        if (getHandle() == null)
            return null;
        return (LivingEntity) getHandle().getBukkitEntity();
    }

    public EntityLiving getHandle() {
        return mcEntity;
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public Trait getTraitFor(Class<? extends Trait> clazz) {
        return CitizensAPI.getTraitFactory().getTrait(clazz);
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    public void load(final DataKey root) {
        // Spawn the NPC
        Spawned spawned = getTrait(Spawned.class);
        CurrentLocation spawnLocation = getTrait(CurrentLocation.class);
        try {
            spawned.load(root.getRelative("spawned"));
            spawnLocation.load(root.getRelative("location"));
            if (spawned.shouldSpawn() && spawnLocation.getLocation() != null)
                spawn(spawnLocation.getLocation());
        } catch (NPCLoadException e) {
        }

        metadata.loadFrom(root.getRelative("metadata"));
        // Load traits

        String traitNames = root.getString("traitnames");
        Iterable<DataKey> keys = traitNames.isEmpty() ? root.getRelative("traits").getSubKeys() : Iterables
                .transform(Splitter.on(',').split(traitNames), new Function<String, DataKey>() {
                    @Override
                    public DataKey apply(@Nullable String input) {
                        return root.getRelative("traits." + input);
                    }
                });
        for (DataKey traitKey : keys) {
            if (traitKey.keyExists("enabled") && !traitKey.getBoolean("enabled"))
                continue;
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
            try {
                trait.load(traitKey);
                PersistenceLoader.load(trait, traitKey);
            } catch (NPCLoadException ex) {
                Messaging.logTr(Messages.TRAIT_LOAD_FAILED, traitKey.name(), getId());
            }
        }

        navigator.load(root.getRelative("navigator"));
    }

    @Override
    public void removeTrait(Class<? extends Trait> clazz) {
        Trait present = traits.get(clazz);
        if (present != null)
            removedTraits.add(present.getName());
        super.removeTrait(clazz);
    }

    private void removeTraitData(DataKey root) {
        for (String name : removedTraits) {
            root.removeKey("traits." + name);
        }
        removedTraits.clear();
    }

    public void save(DataKey root) {
        root.setString("name", getFullName());

        metadata.saveTo(root.getRelative("metadata"));
        navigator.save(root.getRelative("navigator"));

        // Save all existing traits
        StringBuilder traitNames = new StringBuilder();
        for (Trait trait : traits.values()) {
            DataKey traitKey = root.getRelative("traits." + trait.getName());
            trait.save(traitKey);
            PersistenceLoader.save(trait, traitKey);
            removedTraits.remove(trait.getName());
            traitNames.append(trait.getName() + ",");
        }
        if (traitNames.length() > 0) {
            root.setString("traitnames", traitNames.substring(0, traitNames.length() - 1));
        }
        removeTraitData(root);
    }

    @Override
    public boolean spawn(Location loc) {
        Validate.notNull(loc, "location cannot be null");
        if (isSpawned())
            return false;

        mcEntity = createHandle(loc);
        boolean couldSpawn = mcEntity.world.addEntity(mcEntity, SpawnReason.CUSTOM);
        if (!couldSpawn) {
            // we need to wait for a chunk load before trying to spawn
            mcEntity = null;
            EventListen.add(loc, getId());
            return true;
        }

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled()) {
            mcEntity = null;
            return false;
        }

        getBukkitEntity().setMetadata(NPC_METADATA_MARKER,
                new FixedMetadataValue(CitizensAPI.getPlugin(), true));

        // Set the spawned state
        getTrait(CurrentLocation.class).setLocation(loc);
        getTrait(Spawned.class).setSpawned(true);

        navigator.onSpawn();
        // Modify NPC using traits after the entity has been created
        for (Trait trait : traits.values())
            trait.onSpawn();
        return true;
    }

    @Override
    public void update() {
        try {
            super.update();
            if (isSpawned()) {
                NMS.trySwim(getHandle());
                navigator.update();
            }
        } catch (Exception ex) {
            Messaging.logTr(Messages.EXCEPTION_UPDATING_NPC, getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static final String NPC_METADATA_MARKER = "NPC";
}