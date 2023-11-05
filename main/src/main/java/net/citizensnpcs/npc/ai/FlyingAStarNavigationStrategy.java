package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.FlyingBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;

public class FlyingAStarNavigationStrategy extends AbstractPathStrategy {
    private int iterations;
    private final NPC npc;
    private final NavigatorParameters parameters;
    private Path plan;
    private boolean planned;
    private AStarMachine<VectorNode, Path>.AStarState state;
    private final Location target;
    private Vector vector;

    public FlyingAStarNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector> list = Lists.newArrayList(path);
        target = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        parameters = params;
        this.npc = npc;
        setPlan(new Path(list));
    }

    public FlyingAStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        target = dest;
        parameters = params;
        this.npc = npc;
    }

    @Override
    public Location getCurrentDestination() {
        return vector != null ? vector.toLocation(npc.getEntity().getWorld()) : target.clone();
    }

    @Override
    public Iterable<Vector> getPath() {
        return plan == null ? null : plan.getPath();
    }

    @Override
    public Location getTargetAsLocation() {
        return target;
    }

    private void initialisePathfinder() {
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
        Location location = npc.getEntity().getLocation();
        VectorGoal goal = new VectorGoal(target, (float) parameters.pathDistanceMargin());
        state = ASTAR.getStateFor(goal, new VectorNode(goal, location,
                new NMSChunkBlockSource(location, parameters.range()), parameters.examiners()));
    }

    public void setPlan(Path path) {
        plan = path;
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
            if (parameters.debug()) {
                Util.sendBlockChanges(plan.getBlocks(npc.getEntity().getWorld()),
                        Util.getFallbackMaterial("DANDELION", "YELLOW_FLOWER"));
            }
        }
        planned = true;
    }

    @Override
    public void stop() {
        if (plan != null && parameters.debug()) {
            Util.sendBlockChanges(plan.getBlocks(npc.getEntity().getWorld()), null);
        }
        plan = null;
    }

    @Override
    public boolean update() {
        if (!planned) {
            if (state == null) {
                initialisePathfinder();
            }
            int maxIterations = Setting.MAXIMUM_ASTAR_ITERATIONS.asInt();
            int iterationsPerTick = Setting.ASTAR_ITERATIONS_PER_TICK.asInt();
            Path plan = ASTAR.run(state, iterationsPerTick);
            if (plan == null) {
                if (state.isEmpty()) {
                    setCancelReason(CancelReason.STUCK);
                }
                if (iterationsPerTick > 0 && maxIterations > 0) {
                    iterations += iterationsPerTick;
                    if (iterations > maxIterations) {
                        setCancelReason(CancelReason.STUCK);
                    }
                }
            } else {
                setPlan(plan);
            }
        }
        if (getCancelReason() != null || plan == null || plan.isComplete())
            return true;
        Location current = npc.getEntity().getLocation();
        if (current.toVector().distance(vector) <= parameters.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete())
                return true;
            vector = plan.getCurrentVector();
        }
        if (parameters.debug()) {
            npc.getEntity().getWorld().playEffect(vector.toLocation(npc.getEntity().getWorld()), Effect.ENDER_SIGNAL,
                    0);
        }
        if (npc.getEntity().getType() == EntityType.PLAYER) {
            ItemStack stack = ((Player) npc.getEntity()).getInventory().getChestplate();
            try {
                if (stack != null && stack.getType() == Material.ELYTRA
                        && !MinecraftBlockExaminer.canStandOn(current.getBlock().getRelative(BlockFace.DOWN))) {
                    PlayerAnimation.START_ELYTRA.play((Player) npc.getEntity());
                }
            } catch (Exception ex) {
                // 1.8 compatibility
            }
        }
        Vector centeredDest = new Vector(vector.getX() + 0.5D, vector.getY() + 0.1D, vector.getZ() + 0.5D);
        double d0 = centeredDest.getX() - current.getX();
        double d1 = centeredDest.getY() - current.getY();
        double d2 = centeredDest.getZ() - current.getZ();

        Vector velocity = npc.getEntity().getVelocity();
        double motX = velocity.getX(), motY = velocity.getY(), motZ = velocity.getZ();

        motX += (Math.signum(d0) * 0.5D - motX) * 0.1;
        motY += (Math.signum(d1) - motY) * 0.1;
        motZ += (Math.signum(d2) * 0.5D - motZ) * 0.1;
        velocity.setX(motX).setY(motY).setZ(motZ).multiply(parameters.speed());
        npc.getEntity().setVelocity(velocity);

        if (npc.getEntity().getType() != EntityType.ENDER_DRAGON) {
            NMS.setVerticalMovement(npc.getEntity(), 0.5);
            Util.faceLocation(npc.getEntity(), centeredDest.toLocation(npc.getEntity().getWorld()));
        }
        plan.run(npc);
        return false;
    }

    private static AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
}
