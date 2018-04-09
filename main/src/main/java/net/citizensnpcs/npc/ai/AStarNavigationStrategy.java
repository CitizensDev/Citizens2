package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;

public class AStarNavigationStrategy extends AbstractPathStrategy {
    private final Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private boolean planned = false;
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
        plan = null;
    }

    @Override
    public boolean update() {
        if (!planned) {
            Location location = npc.getEntity().getLocation();
            VectorGoal goal = new VectorGoal(destination, (float) params.pathDistanceMargin());
            setPlan(ASTAR.runFully(goal,
                    new VectorNode(goal, location, new ChunkBlockSource(location, params.range()), params.examiners()),
                    Setting.MAXIMUM_ASTAR_ITERATIONS.asInt()));
        }
        if (getCancelReason() != null || plan == null || plan.isComplete()) {
            return true;
        }
        Location currLoc = npc.getEntity().getLocation(NPC_LOCATION);
        if (currLoc.toVector().distanceSquared(vector) <= params.distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete()) {
                return true;
            }
            vector = plan.getCurrentVector();
        }
        double dX = vector.getBlockX() - currLoc.getX();
        double dZ = vector.getBlockZ() - currLoc.getZ();
        double dY = vector.getY() - currLoc.getY();
        double xzDistance = dX * dX + dZ * dZ;
        double distance = xzDistance + dY * dY;
        if (params.debug()) {
            npc.getEntity().getWorld().playEffect(vector.toLocation(npc.getEntity().getWorld()), Effect.ENDER_SIGNAL,
                    0);
        }
        if (distance > 0 && dY > NMS.getStepHeight(npc.getEntity()) && xzDistance <= 2.75) {
            NMS.setShouldJump(npc.getEntity());
        }
        double destX = vector.getX() + 0.5, destZ = vector.getZ() + 0.5;
        Block block = currLoc.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        if (MinecraftBlockExaminer.isDoor(block.getType())) {
            Door door = (Door) block.getState().getData();
            if (door.isOpen()) {
                BlockFace targetFace = door.getFacing().getOppositeFace();
                destX = vector.getX() + targetFace.getModX();
                destZ = vector.getZ() + targetFace.getModZ();
            }
        }
        NMS.setDestination(npc.getEntity(), destX, vector.getY(), destZ, params.speed());
        params.run();
        plan.run(npc);
        return false;
    }

    private static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}
