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
    private int iterations;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private boolean planned = false;
    private AStarMachine<VectorNode, Path>.AStarState state;
    private Vector vector;

    public AStarNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector> list = Lists.newArrayList(path);
        this.params = params;
        this.destination = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        this.npc = npc;
        setPlan(new Path(list));
    }

    public AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.destination = dest;
        this.npc = npc;
    }

    @Override
    public Iterable<Vector> getPath() {
        return plan == null ? null : plan.getPath();
    }

    @Override
    public Location getTargetAsLocation() {
        return destination;
    }

    public void initialisePathfinder() {
        params.examiner(new BlockExaminer() {
            @Override
            public float getCost(BlockSource source, PathPoint point) {
                Vector pos = point.getVector();
                Material in = source.getMaterialAt(pos);
                Material above = source.getMaterialAt(pos.setY(pos.getY() + 1));
                return params.avoidWater() && MinecraftBlockExaminer.isLiquid(in, above) ? 1F : 0F;
            }

            @Override
            public PassableState isPassable(BlockSource source, PathPoint point) {
                return PassableState.IGNORE;
            }
        });
        Location location = npc.getEntity().getLocation();
        VectorGoal goal = new VectorGoal(destination, (float) params.pathDistanceMargin());
        state = ASTAR.getStateFor(goal,
                new VectorNode(goal, location, new NMSChunkBlockSource(location, params.range()), params.examiners()));
    }

    public void setPlan(Path path) {
        this.plan = path;
        this.planned = true;
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
            if (params.debug()) {
                plan.debug();
            }
        }
    }

    @Override
    public void stop() {
        if (plan != null && params.debug()) {
            plan.debugEnd();
        }
        state = null;
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
        if (getCancelReason() != null || plan == null || plan.isComplete()) {
            return true;
        }
        Location currLoc = npc.getEntity().getLocation(NPC_LOCATION);
        Vector destVector = new Vector(vector.getX() + 0.5, vector.getY(), vector.getZ() + 0.5);
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
        double dX = destVector.getX() - currLoc.getX();
        double dZ = destVector.getZ() - currLoc.getZ();
        double dY = destVector.getY() - currLoc.getY();
        double xzDistance = dX * dX + dZ * dZ;
        if ((dY * dY) < 1 && xzDistance <= params.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete()) {
                return true;
            }
            vector = plan.getCurrentVector();
            return false;
        }
        if (params.debug()) {
            npc.getEntity().getWorld().playEffect(destVector.toLocation(npc.getEntity().getWorld()),
                    Effect.ENDER_SIGNAL, 0);
        }
        double distance = xzDistance + dY * dY;

        if (npc.getEntity() instanceof LivingEntity) {
            NMS.setDestination(npc.getEntity(), destVector.getX(), destVector.getY(), destVector.getZ(),
                    params.speed());
        } else {
            Vector dir = destVector.subtract(npc.getEntity().getLocation().toVector()).normalize().multiply(0.2);
            Material in = npc.getEntity().getLocation().getBlock().getType();
            if (distance > 0 && dY >= 1 && xzDistance <= 2.75 || (dY >= 0.2 && MinecraftBlockExaminer.isLiquid(in))) {
                dir.add(new Vector(0, 0.75, 0));
            }
            Util.faceLocation(npc.getEntity(), destVector.toLocation(npc.getEntity().getWorld()));
            npc.getEntity().setVelocity(dir);
        }
        params.run();
        plan.run(npc);
        return false;
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}
