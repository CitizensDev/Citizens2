package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.AbstractPathStrategy;
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

public class FlyingAStarNavigationStrategy extends AbstractPathStrategy {
    private final NPC npc;
    private final NavigatorParameters parameters;
    private Path plan;
    private boolean planned;
    private final Location target;
    private Vector vector;

    public FlyingAStarNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector> list = Lists.newArrayList(path);
        this.target = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        this.parameters = params;
        this.npc = npc;
        setPlan(new Path(list));
    }

    public FlyingAStarNavigationStrategy(final NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.target = dest;
        this.parameters = params;
        this.npc = npc;
    }

    @Override
    public Iterable<Vector> getPath() {
        return plan == null ? null : plan.getPath();
    }

    @Override
    public Location getTargetAsLocation() {
        return target;
    }

    public void setPlan(Path path) {
        this.plan = path;
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
            if (parameters.debug()) {
                plan.debug();
            }
        }
        planned = true;
    }

    @Override
    public void stop() {
        if (plan != null && parameters.debug()) {
            plan.debugEnd();
        }
        plan = null;
    }

    @Override
    public boolean update() {
        if (!planned) {
            Location location = npc.getEntity().getLocation();
            VectorGoal goal = new VectorGoal(target, (float) parameters.pathDistanceMargin());
            boolean found = false;
            for (BlockExaminer examiner : parameters.examiners()) {
                if (examiner instanceof FlyingBlockExaminer) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                parameters.examiner(new FlyingBlockExaminer());
            }
            setPlan(ASTAR.runFully(goal, new VectorNode(goal, location,
                    new ChunkBlockSource(location, parameters.range()), parameters.examiners()), 50000));
        }
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
        if (parameters.debug()) {
            npc.getEntity().getWorld().playEffect(vector.toLocation(npc.getEntity().getWorld()), Effect.ENDER_SIGNAL,
                    0);
        }

        double d0 = vector.getX() + 0.5D - current.getX();
        double d1 = vector.getY() + 0.1D - current.getY();
        double d2 = vector.getZ() + 0.5D - current.getZ();

        Vector velocity = npc.getEntity().getVelocity();
        double motX = velocity.getX(), motY = velocity.getY(), motZ = velocity.getZ();

        motX += (Math.signum(d0) * 0.5D - motX) * 0.1;
        motY += (Math.signum(d1) - motY) * 0.1;
        motZ += (Math.signum(d2) * 0.5D - motZ) * 0.1;
        float targetYaw = (float) (Math.atan2(motZ, motX) * 180.0D / Math.PI) - 90.0F;
        float normalisedTargetYaw = (targetYaw - current.getYaw()) % 360;
        if (normalisedTargetYaw >= 180.0F) {
            normalisedTargetYaw -= 360.0F;
        }
        if (normalisedTargetYaw < -180.0F) {
            normalisedTargetYaw += 360.0F;
        }
        velocity.setX(motX).setY(motY).setZ(motZ).multiply(parameters.speed());
        npc.getEntity().setVelocity(velocity);

        if (npc.getEntity().getType() != EntityType.ENDER_DRAGON) {
            NMS.setVerticalMovement(npc.getEntity(), 0.5);
            float newYaw = current.getYaw() + normalisedTargetYaw;
            current.setYaw(newYaw);
            NMS.setHeadYaw(npc.getEntity(), newYaw);
            npc.teleport(current, TeleportCause.PLUGIN);
        }
        parameters.run();
        plan.run(npc);
        return false;
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}
