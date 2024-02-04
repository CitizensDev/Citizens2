package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.AStarMachine.AStarState;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class AStarNavigationStrategy extends AbstractPathStrategy {
    private final Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private AStarPlanner planner;
    private Vector vector;

    public AStarNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector> list = Lists.newArrayList(path);
        this.params = params;
        destination = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        this.npc = npc;
        plan = new Path(list);
    }

    public AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        destination = dest;
        this.npc = npc;
    }

    @Override
    public Location getCurrentDestination() {
        return vector != null ? vector.toLocation(npc.getEntity().getWorld()) : destination.clone();
    }

    @Override
    public Iterable<Vector> getPath() {
        return plan == null ? null : plan.getPath();
    }

    @Override
    public Location getTargetAsLocation() {
        return destination;
    }

    @Override
    public void stop() {
        if (plan != null && params.debug()) {
            Util.sendBlockChanges(plan.getBlocks(npc.getEntity().getWorld()), null);
        }
        plan = null;
    }

    @Override
    public boolean update() {
        if (plan == null) {
            if (planner == null) {
                planner = new AStarPlanner(params, npc.getEntity().getLocation(), destination);
            }
            CancelReason reason = planner.tick(Setting.ASTAR_ITERATIONS_PER_TICK.asInt(),
                    Setting.MAXIMUM_ASTAR_ITERATIONS.asInt());
            if (reason != null) {
                setCancelReason(reason);
            }
            plan = planner.plan;
            if (plan != null) {
                planner = null;
            }
        }
        if (getCancelReason() != null || plan == null || plan.isComplete())
            return true;
        if (vector == null) {
            vector = plan.getCurrentVector();
        }
        Location loc = npc.getEntity().getLocation();
        /* Proper door movement - gets stuck on corners at times

         Block block = currLoc.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
          if (MinecraftBlockExaminer.isDoor(block.getType())) {
            Door door = (Door) block.getState().getData();
            if (door.isOpen()) {
                BlockFace targetFace = door.getFacing().getOppositeFace();
                destVector.setX(vector.getX() + targetFace.getModX());
                destVector.setZ(vector.getZ() + targetFace.getModZ());
            }
        }*/
        Location dest = Util.getCenterLocation(vector.toLocation(loc.getWorld()).getBlock());
        double dX = dest.getX() - loc.getX();
        double dZ = dest.getZ() - loc.getZ();
        double dY = dest.getY() - loc.getY();
        double xzDistance = Math.sqrt(dX * dX + dZ * dZ);
        if (Math.abs(dY) < 1 && xzDistance <= params.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete())
                return true;
            vector = plan.getCurrentVector();
            return false;
        }
        if (params.debug()) {
            npc.getEntity().getWorld().playEffect(dest, Effect.ENDER_SIGNAL, 0);
        }
        if (npc.getEntity() instanceof LivingEntity && !npc.getEntity().getType().name().contains("ARMOR_STAND")) {
            NMS.setDestination(npc.getEntity(), dest.getX(), dest.getY(), dest.getZ(), params.speed());
        } else {
            Vector dir = dest.toVector().subtract(npc.getEntity().getLocation().toVector()).normalize().multiply(0.2);
            boolean liquidOrInLiquid = MinecraftBlockExaminer.isLiquidOrInLiquid(loc.getBlock());
            if (dY >= 1 && xzDistance <= 0.4 || dY >= 0.2 && liquidOrInLiquid) {
                dir.add(new Vector(0, 0.75, 0));
            }
            npc.getEntity().setVelocity(dir);
            Util.faceLocation(npc.getEntity(), dest);
        }
        plan.run(npc);
        return false;
    }

    public static class AStarPlanner {
        Location from;
        int iterations;
        NavigatorParameters params;
        Path plan;
        AStarState state;
        Location to;

        public AStarPlanner(NavigatorParameters params, Location from, Location to) {
            this.params = params;
            this.from = from;
            this.to = to;
            params.examiner(new BlockExaminer() {
                @Override
                public float getCost(BlockSource source, PathPoint point) {
                    Vector pos = point.getVector();
                    Material above = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ());
                    return params.avoidWater() && (MinecraftBlockExaminer.isLiquid(above)
                            || MinecraftBlockExaminer.isLiquidOrInLiquid(pos.toLocation(source.getWorld()).getBlock()))
                                    ? 2F
                                    : 0F;
                }

                @Override
                public PassableState isPassable(BlockSource source, PathPoint point) {
                    return PassableState.IGNORE;
                }
            });
            VectorGoal goal = new VectorGoal(to, (float) params.pathDistanceMargin());
            state = ASTAR.getStateFor(goal,
                    new VectorNode(goal, from, new NMSChunkBlockSource(from, params.range()), params.examiners()));
        }

        public CancelReason tick(int iterationsPerTick, int maxIterations) {
            if (plan != null)
                return null;
            Path plan = ASTAR.run(state, iterationsPerTick);
            if (plan == null) {
                if (state.isEmpty())
                    return CancelReason.STUCK;
                if (iterationsPerTick > 0 && maxIterations > 0) {
                    iterations += iterationsPerTick;
                    if (iterations > maxIterations)
                        return CancelReason.STUCK;
                }
            } else {
                this.plan = plan;
                if (params.debug()) {
                    Util.sendBlockChanges(plan.getBlocks(to.getWorld()),
                            Util.getFallbackMaterial("DANDELION", "YELLOW_FLOWER"));
                }
            }
            return null;
        }
    }

    private static AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
}
