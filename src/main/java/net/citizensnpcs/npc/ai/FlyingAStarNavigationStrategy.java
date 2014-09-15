package net.citizensnpcs.npc.ai;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.astar.pathfinder.FlyingBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_7_R4.MathHelper;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class FlyingAStarNavigationStrategy extends AbstractPathStrategy {
    private final NPC npc;
    private final NavigatorParameters parameters;
    private Path plan;
    private final Location target;
    private Vector vector;

    public FlyingAStarNavigationStrategy(final NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.target = dest;
        this.parameters = params;
        this.npc = npc;
        Location location = Util.getEyeLocation(npc.getEntity());
        VectorGoal goal = new VectorGoal(dest, (float) params.pathDistanceMargin());
        boolean found = false;
        for (BlockExaminer examiner : params.examiners()) {
            if (examiner instanceof FlyingBlockExaminer) {
                found = true;
                break;
            }
        }
        if (!found) {
            params.examiner(new FlyingBlockExaminer());
        }
        plan = ASTAR.runFully(goal, new VectorNode(goal, location, new ChunkBlockSource(location, params.range()),
                params.examiners()), 50000);
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
            if (Setting.DEBUG_PATHFINDING.asBoolean()) {
                plan.debug();
            }
        }
    }

    @Override
    public Location getTargetAsLocation() {
        return target;
    }

    @Override
    public void stop() {
        if (plan != null && Setting.DEBUG_PATHFINDING.asBoolean()) {
            plan.debugEnd();
        }
        plan = null;
    }

    @Override
    public boolean update() {
        if (getCancelReason() != null || plan == null || plan.isComplete()) {
            return true;
        }
        Location current = npc.getEntity().getLocation(NPC_LOCATION);
        if (current.toVector().distanceSquared(vector) <= parameters.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete()) {
                return true;
            }
            vector = plan.getCurrentVector();
        }

        double d0 = vector.getX() + 0.5D - current.getX();
        double d1 = vector.getY() + 0.1D - current.getY();
        double d2 = vector.getZ() + 0.5D - current.getZ();

        Vector velocity = npc.getEntity().getVelocity();
        double motX = velocity.getX(), motY = velocity.getY(), motZ = velocity.getZ();

        motX += (Math.signum(d0) * 0.5D - motX) * 0.1;
        motY += (Math.signum(d1) * 0.7D - motY) * 0.1;
        motZ += (Math.signum(d2) * 0.5D - motZ) * 0.1;
        float targetYaw = (float) (Math.atan2(motZ, motX) * 180.0D / Math.PI) - 90.0F;
        float normalisedTargetYaw = MathHelper.g(targetYaw - current.getYaw());

        velocity.setX(motX).setY(motY).setZ(motZ).multiply(parameters.speed());
        npc.getEntity().setVelocity(velocity);

        NMS.setVerticalMovement(npc.getEntity(), 0.5);
        if (npc.getEntity().getType() != EntityType.ENDER_DRAGON) {
            NMS.setHeadYaw(NMS.getHandle(npc.getEntity()), current.getYaw() + normalisedTargetYaw);
        }
        parameters.run();
        plan.run(npc);
        return false;
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}
