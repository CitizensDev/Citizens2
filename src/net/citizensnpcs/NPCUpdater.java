package net.citizensnpcs;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;

import net.minecraft.server.EntityLiving;

public class NPCUpdater implements Runnable {
    private final CitizensNPCManager npcManager;

    public NPCUpdater(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        for (NPC npc : npcManager) {
            if (!npc.isSpawned()) {
                Messaging.debug(npc.getName() + " is not spawned.");
                continue;
            }
            Messaging.debug(npc.getName());
            CitizensNPC handle = (CitizensNPC) npc;
            handle.update();

            // This needs to be handled somewhere...is this the best place?
            EntityLiving search = null;
            if ((search = handle.getHandle().world.findNearbyPlayer(handle.getHandle(), 5)) != null
                    && npc.getTrait(LookClose.class).shouldLookClose())
                faceEntity(handle, search.getBukkitEntity());
        }
    }

    private void faceEntity(CitizensNPC npc, Entity target) {
        if (npc.getBukkitEntity().getWorld() != target.getWorld())
            return;
        Location loc = npc.getBukkitEntity().getLocation();

        double xDiff = target.getLocation().getX() - loc.getX();
        double yDiff = target.getLocation().getY() - loc.getY();
        double zDiff = target.getLocation().getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        npc.getHandle().yaw = (float) yaw - 90;
        npc.getHandle().pitch = (float) pitch;
    }
}