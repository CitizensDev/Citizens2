package net.citizensnpcs.trait;

import net.citizensnpcs.Toggleable;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityLiving;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

@SaveId("look-close")
public class LookClose extends Trait implements Runnable, Toggleable {
    private final NPC npc;
    private boolean shouldLookClose;

    public LookClose(NPC npc) {
        this.npc = npc;
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
        npc.getHandle().X = npc.getHandle().yaw;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        shouldLookClose = key.getBoolean("");
    }

    @Override
    public void run() {
        EntityLiving search = null;
        CitizensNPC handle = (CitizensNPC) npc;
        if ((search = handle.getHandle().world.findNearbyPlayer(handle.getHandle(), 5)) != null && shouldLookClose)
            faceEntity(handle, search.getBukkitEntity());
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldLookClose);
    }

    public void setLookClose(boolean shouldLookClose) {
        this.shouldLookClose = shouldLookClose;
    }

    public boolean shouldLookClose() {
        return shouldLookClose;
    }

    @Override
    public void toggle() {
        shouldLookClose = !shouldLookClose;
    }

    @Override
    public String toString() {
        return "LookClose{" + shouldLookClose + "}";
    }
}