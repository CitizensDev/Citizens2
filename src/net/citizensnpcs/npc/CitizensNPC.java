package net.citizensnpcs.npc;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.api.npc.trait.trait.Spawned;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class CitizensNPC extends AbstractNPC {
    private static final double lookRange = 5;
    protected final CitizensNPCManager manager;
    protected net.minecraft.server.Entity mcEntity;

    protected CitizensNPC(CitizensNPCManager manager, int id, String name) {
        super(id, name);
        this.manager = manager;
    }

    protected abstract net.minecraft.server.Entity createHandle(Location loc);

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already despawned.");
            return false;
        }

        Bukkit.getPluginManager().callEvent(new NPCDespawnEvent(this));

        manager.despawn(this);
        mcEntity = null;

        return true;
    }

    // TODO: is this necessary? it's a helper method...
    protected void faceEntity(Location target) {
        if (getBukkitEntity().getWorld() != target.getWorld())
            return;
        Location loc = getBukkitEntity().getLocation();

        double xDiff = target.getX() - loc.getX();
        double yDiff = target.getY() - loc.getY();
        double zDiff = target.getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        mcEntity.yaw = (float) yaw - 90;
        mcEntity.pitch = (float) pitch;
    }

    @Override
    public Entity getBukkitEntity() {
        return getHandle().getBukkitEntity();
    }

    public net.minecraft.server.Entity getHandle() {
        return mcEntity;
    }

    @Override
    public Navigator getNavigator() {
        return new CitizensNavigator(this);
    }

    @Override
    public boolean isSpawned() {
        return getHandle() != null;
    }

    @Override
    public void remove() {
        if (isSpawned())
            despawn();
        manager.remove(this);
    }

    @Override
    public boolean spawn(Location loc) {
        if (isSpawned()) {
            Messaging.debug("The NPC with the ID '" + getId() + "' is already spawned.");
            return false;
        }

        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, loc);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        mcEntity = createHandle(loc);
        mcEntity.world.addEntity(mcEntity);

        // Set the location
        addTrait(new SpawnLocation(loc));
        // Set the spawned state
        addTrait(new Spawned(true));
        return true;
    }

    public void tick() {
        // TODO: this needs to be less hard-coded... does everyone want this
        // behaviour?
        if (mcEntity != null) {
            if (getTrait(LookClose.class).shouldLookClose()
                    && mcEntity.world.findNearbyPlayer(mcEntity, lookRange) != null)
                faceEntity(mcEntity.world.findNearbyPlayer(mcEntity, lookRange).getBukkitEntity().getLocation());
        }
    }
}