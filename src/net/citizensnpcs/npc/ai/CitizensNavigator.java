package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.ai.NavigatorCallback;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.trait.LookClose;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class CitizensNavigator implements Navigator {
    private final CitizensNPC npc;
    private PathStrategy executing;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void registerCallback(NavigatorCallback callback) {
    }

    public void update() {
        if (npc.getHandle() != null && npc.getHandle().world.findNearbyPlayer(npc.getHandle(), 5) != null)
            if (npc.getTrait(LookClose.class).shouldLookClose()
                    && npc.getHandle().world.findNearbyPlayer(npc.getHandle(), 5) != null)
                faceEntity(npc.getHandle().world.findNearbyPlayer(npc.getHandle(), 5).getBukkitEntity());
        if (executing != null)
            executing.update();
    }

    @Override
    public void setDestination(Location destination) {
        executing = new MoveStrategy(npc, destination);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        executing = new TargetStrategy(npc, target, aggressive);
    }

    private void faceEntity(Entity target) {
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