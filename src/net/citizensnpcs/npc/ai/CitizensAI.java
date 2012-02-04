package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.ai.AI;
import net.citizensnpcs.api.npc.ai.Goal;
import net.citizensnpcs.api.npc.ai.NavigationCallback;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class CitizensAI implements AI {
    private PathStrategy executing;
    private Runnable ai;
    private final CitizensNPC npc;

    public CitizensAI(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void addGoal(int priority, Goal goal) {
        // TODO Auto-generated method stub

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

    @Override
    public void registerNavigationCallback(NavigationCallback callback) {
    }

    @Override
    public void setAI(Runnable ai) {
        this.ai = ai;
    }

    @Override
    public void setDestination(Location destination) {
        executing = new MoveStrategy(npc, destination);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        executing = new TargetStrategy(npc, target, aggressive);
    }

    public void update() {
        if (executing != null && executing.update()) {
            executing = null;
        }

        if (ai != null) {
            try {
                ai.run();
            } catch (Throwable ex) {
                Messaging.log("Unexpected error while running ai " + ai);
                ex.printStackTrace();
            }
        }
    }
}