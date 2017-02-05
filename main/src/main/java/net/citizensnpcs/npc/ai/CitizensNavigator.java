package net.citizensnpcs.npc.ai;

import java.util.Iterator;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.PathStrategy;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class CitizensNavigator implements Navigator, Runnable {
    private final NavigatorParameters defaultParams = new NavigatorParameters().baseSpeed(UNINITIALISED_SPEED)
            .range(Setting.DEFAULT_PATHFINDING_RANGE.asFloat()).debug(Setting.DEBUG_PATHFINDING.asBoolean())
            .defaultAttackStrategy(MCTargetStrategy.DEFAULT_ATTACK_STRATEGY)
            .attackRange(Setting.NPC_ATTACK_DISTANCE.asDouble())
            .updatePathRate(Setting.DEFAULT_PATHFINDER_UPDATE_PATH_RATE.asInt())
            .distanceMargin(Setting.DEFAULT_DISTANCE_MARGIN.asDouble())
            .stationaryTicks(Setting.DEFAULT_STATIONARY_TICKS.asInt()).stuckAction(TeleportStuckAction.INSTANCE)
            .examiner(new MinecraftBlockExaminer()).useNewPathfinder(Setting.USE_NEW_PATHFINDER.asBoolean());
    private PathStrategy executing;
    private int lastX, lastY, lastZ;
    private NavigatorParameters localParams = defaultParams;
    private final NPC npc;
    private boolean paused;
    private int stationaryTicks;

    public CitizensNavigator(NPC npc) {
        this.npc = npc;
        if (Setting.NEW_PATHFINDER_OPENS_DOORS.asBoolean()) {
            defaultParams.examiner(new DoorExaminer());
        }
    }

    @Override
    public void cancelNavigation() {
        stopNavigating(CancelReason.PLUGIN);
    }

    @Override
    public NavigatorParameters getDefaultParameters() {
        return defaultParams;
    }

    @Override
    public EntityTarget getEntityTarget() {
        return executing instanceof EntityTarget ? (EntityTarget) executing : null;
    }

    @Override
    public NavigatorParameters getLocalParameters() {
        if (!isNavigating()) {
            return defaultParams;
        }
        return localParams;
    }

    @Override
    public NPC getNPC() {
        return npc;
    }

    @Override
    public PathStrategy getPathStrategy() {
        return executing;
    }

    @Override
    public Location getTargetAsLocation() {
        return isNavigating() ? executing.getTargetAsLocation() : null;
    }

    @Override
    public TargetType getTargetType() {
        return isNavigating() ? executing.getTargetType() : null;
    }

    @Override
    public boolean isNavigating() {
        return executing != null;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    public void load(DataKey root) {
        if (root.keyExists("pathfindingrange")) {
            defaultParams.range((float) root.getDouble("pathfindingrange"));
        }
        if (root.keyExists("stationaryticks")) {
            defaultParams.stationaryTicks(root.getInt("stationaryticks"));
        }
        if (root.keyExists("distancemargin")) {
            defaultParams.distanceMargin(root.getDouble("distancemargin"));
        }
        if (root.keyExists("updatepathrate")) {
            defaultParams.updatePathRate(root.getInt("updatepathrate"));
        }
        defaultParams.speedModifier((float) root.getDouble("speedmodifier", 1F));
        defaultParams.avoidWater(root.getBoolean("avoidwater"));
        if (!root.getBoolean("usedefaultstuckaction") && defaultParams.stuckAction() == TeleportStuckAction.INSTANCE) {
            defaultParams.stuckAction(null);
        }
    }

    public void onDespawn() {
        stopNavigating(CancelReason.NPC_DESPAWNED);
    }

    public void onSpawn() {
        if (defaultParams.baseSpeed() == UNINITIALISED_SPEED) {
            defaultParams.baseSpeed(NMS.getSpeedFor(npc));
        }
        updatePathfindingRange();
    }

    @Override
    public void run() {
        updateMountedStatus();
        if (!isNavigating() || !npc.isSpawned() || paused)
            return;
        if (!npc.getStoredLocation().getWorld().equals(getTargetAsLocation().getWorld())
                || Math.pow(localParams.range(), 2) < npc.getStoredLocation().distanceSquared(getTargetAsLocation())) {
            stopNavigating(CancelReason.STUCK);
            return;
        }
        if (updateStationaryStatus())
            return;
        updatePathfindingRange();
        boolean finished = executing.update();
        if (localParams.lookAtFunction() != null) {
            Util.faceLocation(npc.getEntity(), localParams.lookAtFunction().apply(this), true, true);
            Entity entity = npc.getEntity().getPassenger();
            Location npcLoc = npc.getEntity().getLocation();
            while (entity != null) {
                Location loc = entity.getLocation(STATIONARY_LOCATION);
                loc.setYaw(npcLoc.getYaw());
                entity.teleport(loc);
                entity = entity.getPassenger();
            }
        }
        if (!finished) {
            return;
        }
        if (executing.getCancelReason() != null) {
            stopNavigating(executing.getCancelReason());
        } else {
            NavigationCompleteEvent event = new NavigationCompleteEvent(this);
            PathStrategy old = executing;
            Bukkit.getPluginManager().callEvent(event);
            if (old == executing) {
                stopNavigating(null);
            }
        }
    }

    public void save(DataKey root) {
        if (defaultParams.range() != Setting.DEFAULT_PATHFINDING_RANGE.asFloat()) {
            root.setDouble("pathfindingrange", defaultParams.range());
        } else {
            root.removeKey("pathfindingrange");
        }
        if (defaultParams.stationaryTicks() != Setting.DEFAULT_STATIONARY_TICKS.asInt()) {
            root.setInt("stationaryticks", defaultParams.stationaryTicks());
        } else {
            root.removeKey("stationaryticks");
        }
        if (defaultParams.distanceMargin() != Setting.DEFAULT_DISTANCE_MARGIN.asDouble()) {
            root.setDouble("distancemargin", defaultParams.distanceMargin());
        } else {
            root.removeKey("distancemargin");
        }
        if (defaultParams.updatePathRate() != Setting.DEFAULT_PATHFINDER_UPDATE_PATH_RATE.asInt()) {
            root.setInt("updatepathrate", defaultParams.updatePathRate());
        } else {
            root.removeKey("updatepathrate");
        }
        root.setDouble("speedmodifier", defaultParams.speedModifier());
        root.setBoolean("avoidwater", defaultParams.avoidWater());
        root.setBoolean("usedefaultstuckaction", defaultParams.stuckAction() == TeleportStuckAction.INSTANCE);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void setTarget(Entity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
        switchParams();
        updatePathfindingRange();
        PathStrategy newStrategy = new MCTargetStrategy(npc, target, aggressive, localParams);
        switchStrategyTo(newStrategy);
    }

    @Override
    public void setTarget(Iterable<Vector> path) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (path == null || Iterables.size(path) == 0) {
            cancelNavigation();
            return;
        }
        switchParams();
        updatePathfindingRange();
        PathStrategy newStrategy;
        if (npc.isFlyable()) {
            newStrategy = new FlyingAStarNavigationStrategy(npc, path, localParams);
        } else if (localParams.useNewPathfinder() || !(npc.getEntity() instanceof LivingEntity)) {
            newStrategy = new AStarNavigationStrategy(npc, path, localParams);
        } else {
            newStrategy = new MCNavigationStrategy(npc, path, localParams);
        }
        switchStrategyTo(newStrategy);
    }

    @Override
    public void setTarget(Location target) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
        switchParams();
        updatePathfindingRange();
        PathStrategy newStrategy;
        if (npc.isFlyable()) {
            newStrategy = new FlyingAStarNavigationStrategy(npc, target, localParams);
        } else if (localParams.useNewPathfinder() || !(npc.getEntity() instanceof LivingEntity)) {
            newStrategy = new AStarNavigationStrategy(npc, target, localParams);
        } else {
            newStrategy = new MCNavigationStrategy(npc, target, localParams);
        }
        switchStrategyTo(newStrategy);
    }

    private void stopNavigating() {
        if (executing != null) {
            executing.stop();
        }
        executing = null;
        localParams = defaultParams;
        stationaryTicks = 0;
        if (npc.isSpawned()) {
            Vector velocity = npc.getEntity().getVelocity();
            velocity.setX(0).setY(0).setZ(0);
            npc.getEntity().setVelocity(velocity);
        }
    }

    private void stopNavigating(CancelReason reason) {
        if (!isNavigating())
            return;
        Iterator<NavigatorCallback> itr = localParams.callbacks().iterator();
        while (itr.hasNext()) {
            itr.next().onCompletion(reason);
            itr.remove();
        }
        if (reason == null) {
            stopNavigating();
            return;
        }
        if (reason == CancelReason.STUCK) {
            StuckAction action = localParams.stuckAction();
            NavigationStuckEvent event = new NavigationStuckEvent(this, action);
            Bukkit.getPluginManager().callEvent(event);
            action = event.getAction();
            boolean shouldContinue = action != null ? action.run(npc, this) : false;
            if (shouldContinue) {
                stationaryTicks = 0;
                executing.clearCancelReason();
                return;
            }
        }
        NavigationCancelEvent event = new NavigationCancelEvent(this, reason);
        PathStrategy old = executing;
        Bukkit.getPluginManager().callEvent(event);
        if (old == executing) {
            stopNavigating();
        }
    }

    private void switchParams() {
        localParams = defaultParams.clone();
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (executing != null) {
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        }
        executing = newStrategy;
        stationaryTicks = 0;
        if (npc.isSpawned()) {
            NMS.updateNavigationWorld(npc.getEntity(), npc.getEntity().getWorld());
        }
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    private void updateMountedStatus() {
        if (!isNavigating())
            return;
        Entity vehicle = NMS.getVehicle(npc.getEntity());
        if (!(vehicle instanceof NPCHolder)) {
            return;
        }
        NPC mount = ((NPCHolder) vehicle).getNPC();
        switch (getTargetType()) {
            case ENTITY:
                mount.getNavigator().setTarget(getEntityTarget().getTarget(), getEntityTarget().isAggressive());
                break;
            case LOCATION:
                mount.getNavigator().setTarget(getTargetAsLocation());
                break;
            default:
                return;
        }
        cancelNavigation();
    }

    private void updatePathfindingRange() {
        NMS.updatePathfindingRange(npc, localParams.range());
    }

    private boolean updateStationaryStatus() {
        if (localParams.stationaryTicks() < 0)
            return false;
        Location current = npc.getEntity().getLocation(STATIONARY_LOCATION);
        if (current.getY() < -5) {
            stopNavigating(CancelReason.STUCK);
            return true;
        }
        if (lastX == current.getBlockX() && lastY == current.getBlockY() && lastZ == current.getBlockZ()) {
            if (++stationaryTicks >= localParams.stationaryTicks()) {
                stopNavigating(CancelReason.STUCK);
                return true;
            }
        } else
            stationaryTicks = 0;
        lastX = current.getBlockX();
        lastY = current.getBlockY();
        lastZ = current.getBlockZ();
        return false;
    }

    public static class DoorExaminer implements BlockExaminer {
        @Override
        public float getCost(BlockSource source, PathPoint point) {
            return 0F;
        }

        @Override
        public PassableState isPassable(BlockSource source, PathPoint point) {
            Material in = source.getMaterialAt(point.getVector());
            if (MinecraftBlockExaminer.isDoor(in)) {
                point.addCallback(new DoorOpener());
                return PassableState.PASSABLE;
            }
            return PassableState.IGNORE;
        }
    }

    private static class DoorOpener implements PathCallback {
        @Override
        public void run(NPC npc, Block point, ListIterator<Block> path) {
            BlockState state = point.getState();
            Door door = (Door) state.getData();
            if (npc.getStoredLocation().distance(point.getLocation()) < 2) {
                boolean bottom = !door.isTopHalf();
                Block set = bottom ? point : point.getRelative(BlockFace.DOWN);
                state = set.getState();
                door = (Door) state.getData();
                door.setOpen(true);
                state.setData(door);
                state.update();
            }
        }
    }

    private static final Location STATIONARY_LOCATION = new Location(null, 0, 0, 0);

    private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;
}
