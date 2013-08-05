package net.citizensnpcs.npc;

import java.util.Collection;

import net.citizensnpcs.NPCNeedsRespawnEvent;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

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
    public void faceLocation(Location location) {
        if (!isSpawned())
            return;
        Util.faceLocation(getBukkitEntity(), location);
    }

    @Override
    public LivingEntity getBukkitEntity() {
        return entityController == null ? null : entityController.getBukkitEntity();
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public Location getStoredLocation() {
        return isSpawned() ? getBukkitEntity().getLocation() : getTrait(CurrentLocation.class).getLocation();
    }

    @Override
    public boolean isSpawned() {
        return getBukkitEntity() != null;
    }

    @Override
    public void load(final DataKey root) {
        super.load(root);

        // Spawn the NPC
        CurrentLocation spawnLocation = getTrait(CurrentLocation.class);
        if (getTrait(Spawned.class).shouldSpawn() && spawnLocation.getLocation() != null)
            spawn(spawnLocation.getLocation());

        navigator.load(root.getRelative("navigator"));
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
        if (wasSpawned) {
            spawn(prev);
        }
    }

    @Override
    public boolean spawn(Location at) {
        Preconditions.checkNotNull(at, "location cannot be null");
        if (isSpawned()) {
            Messaging.debug("Tried to spawn", getId(), "while already spawned.");
            return false;
        }

        at = at.clone();
        entityController.spawn(at, this);
        EntityLiving mcEntity = ((CraftLivingEntity) getBukkitEntity()).getHandle();
        boolean couldSpawn = !Util.isLoaded(at) ? false : mcEntity.world.addEntity(mcEntity, SpawnReason.CUSTOM);
        mcEntity.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
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
        Collection<Trait> onSpawn = traits.values();
        // work around traits modifying the map during this iteration.
        for (Trait trait : onSpawn.toArray(new Trait[onSpawn.size()])) {
            try {
                trait.onSpawn();
            } catch (Throwable ex) {
                Messaging.severeTr(Messages.TRAIT_ONSPAWN_FAILED, trait.getName(), getId());
                ex.printStackTrace();
            }
        }
        getBukkitEntity().setRemoveWhenFarAway(false);
        getBukkitEntity().setCustomName(getFullName());
        return true;
    }

    private void teleport(final Entity entity, Location location, boolean loaded, int delay) {
        if (!loaded)
            location.getBlock().getChunk();
        final Entity passenger = entity.getPassenger();
        entity.eject();
        entity.teleport(location);
        if (passenger == null)
            return;
        teleport(passenger, location, true, delay++);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                NMS.mount(entity, passenger);
            }
        };
        if (!location.getWorld().equals(entity.getWorld())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), task, delay);
        } else {
            task.run();
        }
    }

    @Override
    public void teleport(Location location, TeleportCause cause) {
        if (!this.isSpawned())
            return;
        teleport(NMS.getRootVehicle(getBukkitEntity()), location, false, 5);
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
            Throwable error = Throwables.getRootCause(ex);
            Messaging.logTr(Messages.EXCEPTION_UPDATING_NPC, getId(), error.getMessage());
            error.printStackTrace();
        }
    }

    private static final String NPC_METADATA_MARKER = "NPC";
}