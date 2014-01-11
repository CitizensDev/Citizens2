package net.citizensnpcs.npc.ai;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AStarNavigationStrategy extends AbstractPathStrategy {
    private final Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private Vector vector;

    AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.destination = dest;
        this.npc = npc;
        Location location = Util.getEyeLocation(npc.getEntity());
        plan = ASTAR.runFully(new VectorGoal(dest, (float) params.distanceMargin()), new VectorNode(location,
                new ChunkBlockSource(location, params.range()), params.examiners()), 50000);
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
        }
    }

    @Override
    public Location getTargetAsLocation() {
        return destination;
    }

    @Override
    public void stop() {
        plan = null;
    }

    @Override
    public boolean update() {
        if (getCancelReason() != null || plan == null || plan.isComplete()) {
            return true;
        }
        if (npc.getEntity().getLocation(NPC_LOCATION).toVector().distanceSquared(vector) <= params.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete()) {
                return true;
            }
            vector = plan.getCurrentVector();
        }
        net.minecraft.server.v1_7_R1.Entity handle = NMS.getHandle(npc.getEntity());
        double dX = vector.getBlockX() - handle.locX;
        double dZ = vector.getBlockZ() - handle.locZ;
        double dY = vector.getY() - handle.locY;
        double xzDistance = dX * dX + dZ * dZ;
        double distance = xzDistance + dY * dY;
        if (Setting.DEBUG_PATHFINDING.asBoolean()) {
            for (int i = 0; i < 5; i++) {
                npc.getEntity().getWorld().playEffect(npc.getStoredLocation(), Effect.MOBSPAWNER_FLAMES, 0);
            }
        }
        if (distance > 0 && dY > 0 && xzDistance <= 2.75) {
            NMS.setShouldJump(npc.getEntity());
        }
        NMS.setDestination(npc.getEntity(), vector.getX(), vector.getY(), vector.getZ(), params.speed());
        params.tick();
        return false;
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}