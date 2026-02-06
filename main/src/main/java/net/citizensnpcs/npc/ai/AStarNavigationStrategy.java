package net.citizensnpcs.npc.ai;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.PathfinderType;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.AStarMachine.AStarState;
import net.citizensnpcs.api.astar.pathfinder.AsyncChunkCache.PathRequest;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.SwimmingNeighbourExaminer;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class AStarNavigationStrategy extends AbstractPathStrategy {
    private Location current;
    private final Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private PathPlanner planner;

    public AStarNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.npc = npc;
        List<Vector> list = Lists.newArrayList(path);
        destination = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        plan = new Path(list);
    }

    public AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.npc = npc;
        if (MinecraftBlockExaminer.isWaterMob(npc.getEntity())) {
            params.examiner(new SwimmingNeighbourExaminer());
        }
        if (!MinecraftBlockExaminer.canStandIn(dest.getBlock())) {
            dest = MinecraftBlockExaminer.findValidLocationAbove(dest, 2);
        }
        destination = dest;
        // TODO: simplify
        if (params.pathfinderType() == PathfinderType.CITIZENS_ASYNC) {
            planner = new AsyncAStarPlanner(CitizensAPI.getAsyncChunkCache()
                    .findPathAsync(new PathRequest(npc.getEntity().getLocation(), dest, 1, params)));
        } else {
            planner = new AStarPlanner(params, npc.getEntity().getLocation(), destination);
        }
    }

    @Override
    public Location getCurrentDestination() {
        return current != null ? current : destination.clone();
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
        if (planner != null) {
            CancelReason reason = planner.tick();
            if (reason != null) {
                setCancelReason(reason);
                return true;
            }
            if (planner.getPath() == null)
                return false;
            plan = planner.getPath();
            if (plan != null && params.debug()) {
                Util.sendBlockChanges(plan.getBlocks(destination.getWorld()),
                        Util.getFallbackMaterial("DANDELION", "YELLOW_FLOWER"));
            }
            planner = null;
        }
        if (getCancelReason() != null || plan == null || plan.isComplete())
            return true;
        Location loc = npc.getEntity().getLocation();
        if (current == null) {
            current = plan.getCurrentVector().toLocation(loc.getWorld());
        }
        /* Proper door movement - gets stuck on corners at times

        Block block = loc.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
         if (MinecraftBlockExaminer.isDoor(block.getType())) {
           Door door = (Door) block.getState().getData();
           if (door.isOpen()) {
           BlockFace targetFace = door.getFacing().getOppositeFace();
           dest.setX(vector.getX() + targetFace.getModX());
           dest.setZ(vector.getZ() + targetFace.getModZ());
           }
        }*/
        if (params.withinMargin(loc, current)) {
            plan.update(npc);
            if (plan.isComplete())
                return true;
            current = null;
            return false;
        }
        if (params.debug()) {
            npc.getEntity().getWorld().playEffect(current, Effect.ENDER_SIGNAL, 0);
        }
        if (npc.getEntity() instanceof LivingEntity && npc.getEntity().getType() != EntityType.ARMOR_STAND) {
            NMS.setDestination(npc.getEntity(), current.getX(), current.getY(), current.getZ(), params.speedModifier());
        } else {
            Vector dir = current.toVector().subtract(loc.toVector()).normalize().multiply(0.2 * params.speedModifier());
            boolean liquidOrInLiquid = MinecraftBlockExaminer.isLiquidOrWaterlogged(loc.getBlock());
            double dX = current.getX() - loc.getX();
            double dY = current.getY() - loc.getY();
            double dZ = current.getZ() - loc.getZ();
            double xzDistance = Math.sqrt(dX * dX + dZ * dZ);
            if (dY >= 1 && xzDistance <= 0.4 || dY >= 0.2 && liquidOrInLiquid) {
                dir.add(new Vector(0, 0.75, 0));
            }
            npc.getEntity().setVelocity(dir);
            Util.faceLocation(npc.getEntity(), current);
        }
        plan.run(npc);
        return false;
    }

    public static class AStarPlanner implements PathPlanner {
        int iterations;
        int iterationsPerTick = Setting.CITIZENS_PATHFINDER_ASTAR_ITERATIONS_PER_TICK.asInt();
        int maxIterations = Setting.CITIZENS_PATHFINDER_MAXIMUM_ASTAR_ITERATIONS.asInt();
        NavigatorParameters params;
        Path plan;
        AStarState<VectorNode> state;

        public AStarPlanner(NavigatorParameters params, Location from, Location to) {
            this.params = params;
            params.examiner(new BlockExaminer() {
                @Override
                public float getCost(BlockSource source, PathPoint point) {
                    Vector pos = point.getVector();
                    Material above = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ());
                    return params.avoidWater() && (MinecraftBlockExaminer.isLiquid(above) || MinecraftBlockExaminer
                            .isLiquidOrWaterlogged(source.getMaterialAt(pos), source.getBlockDataAt(pos))) ? 2F : 0F;
                }

                @Override
                public PassableState isPassable(BlockSource source, PathPoint point) {
                    return PassableState.IGNORE;
                }
            });
            VectorGoal goal = new VectorGoal(to, (float) params.pathDistanceMargin());
            state = ASTAR.getStateFor(goal,
                    new VectorNode(goal, from, new NMSChunkBlockSource(from, params.range()), params));
        }

        @Override
        public Path getPath() {
            return plan;
        }

        @Override
        public CancelReason tick() {
            plan = ASTAR.run(state, iterationsPerTick);
            if (plan != null)
                return null;
            if (state.isEmpty())
                return CancelReason.STUCK;
            if (iterationsPerTick > 0 && maxIterations > 0) {
                iterations += iterationsPerTick;
                if (iterations > maxIterations)
                    return CancelReason.STUCK;
            }
            return null;
        }
    }

    private static class AsyncAStarPlanner implements PathPlanner {
        private final CompletableFuture<Path> future;
        private Path path;

        public AsyncAStarPlanner(CompletableFuture<Path> future) {
            this.future = future;
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public CancelReason tick() {
            if (future.isDone()) {
                try {
                    path = future.join();
                } catch (CancellationException cancel) {
                    Messaging.debug("Async navigation cancelled");
                    return null;
                } catch (Exception exception) {
                    if (Messaging.isDebugging()) {
                        exception.printStackTrace();
                    }
                    return CancelReason.STUCK;
                }
            }
            return null;
        }
    }

    // TODO: lift this into a navigationstrategy rather than this arbitrary interface
    public static interface PathPlanner {
        public Path getPath();

        public CancelReason tick();
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithVectorStorage();
}
