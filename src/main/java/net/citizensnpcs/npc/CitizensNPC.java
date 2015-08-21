package net.citizensnpcs.npc;

import java.util.Collection;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import net.citizensnpcs.NPCNeedsRespawnEvent;
import net.citizensnpcs.Settings;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.CitizensBlockBreaker;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class CitizensNPC extends AbstractNPC {
    private EntityController entityController;
    private final CitizensNavigator navigator = new CitizensNavigator(this);

    public CitizensNPC(UUID uuid, int id, String name, EntityController entityController, NPCRegistry registry) {
        super(uuid, id, name, registry);
        Preconditions.checkNotNull(entityController);
        this.entityController = entityController;
    }

    @Override
    public boolean despawn(DespawnReason reason) {
        if (!isSpawned()) {
            Messaging.debug("Tried to despawn", getId(), "while already despawned.");
            if (reason == DespawnReason.REMOVAL) {
                Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this, reason));
            }
            return false;
        }

        NPCDespawnEvent event = new NPCDespawnEvent(this, reason);
        if (reason == DespawnReason.CHUNK_UNLOAD) {
            event.setCancelled(Setting.KEEP_CHUNKS_LOADED.asBoolean());
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            getEntity().getLocation().getChunk();
            Messaging.debug("Couldn't despawn", getId(), "due to despawn event cancellation. Force loaded chunk.");
            return false;
        }
        boolean keepSelected = getTrait(Spawned.class).shouldSpawn();
        if (!keepSelected) {
            data().remove("selectors");
        }
        for (Trait trait : traits.values()) {
            trait.onDespawn();
        }
        navigator.onDespawn();
        entityController.remove();

        return true;
    }

    @Override
    public void faceLocation(Location location) {
        if (!isSpawned())
            return;
        Util.faceLocation(getEntity(), location);
    }

    @Override
    public BlockBreaker getBlockBreaker(Block targetBlock, BlockBreakerConfiguration config) {
        return new CitizensBlockBreaker(getEntity(), targetBlock, config);
    }

    @Override
    public Entity getEntity() {
        return entityController == null ? null : entityController.getBukkitEntity();
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public Location getStoredLocation() {
        return isSpawned() ? getEntity().getLocation() : getTrait(CurrentLocation.class).getLocation();
    }

    @Override
    public boolean isFlyable() {
        updateFlyableState();
        return super.isFlyable();
    }

    @Override
    public void load(final DataKey root) {
        super.load(root);

        // Spawn the NPC
        CurrentLocation spawnLocation = getTrait(CurrentLocation.class);
        if (getTrait(Spawned.class).shouldSpawn() && spawnLocation.getLocation() != null) {
            spawn(spawnLocation.getLocation());
        }

        navigator.load(root.getRelative("navigator"));
    }

    @Override
    public void save(DataKey root) {
        super.save(root);
        if (!data().get(NPC.SHOULD_SAVE_METADATA, true))
            return;
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
            prev = getEntity().getLocation();
            despawn(DespawnReason.PENDING_RESPAWN);
        }
        entityController = newController;
        if (wasSpawned) {
            spawn(prev);
        }
    }

    @Override
    public void setFlyable(boolean flyable) {
        super.setFlyable(flyable);
        updateFlyableState();
    }

    @Override
    public boolean spawn(Location at) {
        Preconditions.checkNotNull(at, "location cannot be null");
        if (isSpawned()) {
            Messaging.debug("Tried to spawn", getId(), "while already spawned.");
            return false;
        }
        data().get(NPC.DEFAULT_PROTECTED_METADATA, true);

        at = at.clone();
        getTrait(CurrentLocation.class).setLocation(at);

        entityController.spawn(at, this);

        net.minecraft.server.v1_8_R3.Entity mcEntity = ((CraftEntity) getEntity()).getHandle();
        boolean couldSpawn = !Util.isLoaded(at) ? false : mcEntity.world.addEntity(mcEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        // send skin packets, if applicable, before other NMS packets are sent
        SkinnableEntity skinnable = NMS.getSkinnableNPC(getEntity());
        if (skinnable != null) {
            final double viewDistance = Settings.Setting.NPC_SKIN_VIEW_DISTANCE.asDouble();
            skinnable.getSkinTracker().updateNearbyViewers(viewDistance);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {

                    if (getEntity() == null || !getEntity().isValid())
                        return;

                    SkinnableEntity npc = NMS.getSkinnableNPC(getEntity());
                    if (npc == null)
                        return;

                    npc.getSkinTracker().updateNearbyViewers(viewDistance);
                }
            }, 20);
        }

        mcEntity.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());

        if (!couldSpawn) {
            Messaging.debug("Retrying spawn of", getId(), "later due to chunk being unloaded.",
                    Util.isLoaded(at) ? "Util.isLoaded true" : "Util.isLoaded false");
            // we need to wait for a chunk load before trying to spawn
            entityController.remove();
            Bukkit.getPluginManager().callEvent(new NPCNeedsRespawnEvent(this, at));
            return false;
        }

        NMS.setHeadYaw(mcEntity, at.getYaw());

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, at);
        Bukkit.getPluginManager().callEvent(spawnEvent);

        if (spawnEvent.isCancelled()) {
            entityController.remove();
            Messaging.debug("Couldn't spawn", getId(), "due to event cancellation.");
            return false;
        }

        getEntity().setMetadata(NPC_METADATA_MARKER, new FixedMetadataValue(CitizensAPI.getPlugin(), true));

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

        if (getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) getEntity();
            entity.setRemoveWhenFarAway(false);
            entity.setCustomName(getFullName());

            if (NMS.getStepHeight(entity) < 1) {
                NMS.setStepHeight(NMS.getHandle(entity), 1);
            }

            if (getEntity() instanceof Player) {
                final CraftPlayer player = (CraftPlayer) getEntity();
                NMS.replaceTrackerEntry(player);
            }
        }

        return true;
    }

    @Override
    public void update() {
        try {
            super.update();
            if (!isSpawned())
                return;
            if (data().get(NPC.SWIMMING_METADATA, true)) {
                NMS.trySwim(getEntity());
            }
            navigator.run();

            if (!getNavigator().isNavigating()
                    && getEntity().getWorld().getFullTime() % Setting.PACKET_UPDATE_DELAY.asInt() == 0) {
                if (getEntity() instanceof LivingEntity) {
                    ((LivingEntity) getEntity()).setCustomName(getFullName());
                }
                Player player = getEntity() instanceof Player ? (Player) getEntity() : null;
                NMS.sendPacketNearby(player, getStoredLocation(),
                        new PacketPlayOutEntityTeleport(NMS.getHandle(getEntity())));
            }

            if (getEntity() instanceof LivingEntity) {
                boolean nameplateVisible = data().get(NPC.NAMEPLATE_VISIBLE_METADATA, true);
                ((LivingEntity) getEntity()).setCustomNameVisible(nameplateVisible);
                Byte toByte = Byte.valueOf((byte) (nameplateVisible ? 1 : 0));
                try {
                    ((CraftLivingEntity) getEntity()).getHandle().getDataWatcher().watch(3, toByte);
                } catch (NullPointerException e) {
                    ((CraftLivingEntity) getEntity()).getHandle().getDataWatcher().a(3, toByte);
                }
            }
        } catch (Exception ex) {
            Throwable error = Throwables.getRootCause(ex);
            Messaging.logTr(Messages.EXCEPTION_UPDATING_NPC, getId(), error.getMessage());
            error.printStackTrace();
        }
    }

    private void updateFlyableState() {
        EntityType type = getTrait(MobType.class).getType();
        if (type == null)
            return;
        if (Util.isAlwaysFlyable(type)) {
            data().setPersistent(NPC.FLYABLE_METADATA, true);
        }
    }

    private static final String NPC_METADATA_MARKER = "NPC";
}
