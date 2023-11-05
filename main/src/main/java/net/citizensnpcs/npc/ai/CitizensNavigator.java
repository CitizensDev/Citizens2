package net.citizensnpcs.npc.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
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
import net.citizensnpcs.api.astar.pathfinder.DoorExaminer;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.SwimmingExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.npc.ai.AStarNavigationStrategy.AStarPlanner;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.RotationTrait.PacketRotationSession;
import net.citizensnpcs.util.ChunkCoord;
import net.citizensnpcs.util.NMS;

public class CitizensNavigator implements Navigator, Runnable {
    private Location activeTicket;
    private final NavigatorParameters defaultParams = new NavigatorParameters().baseSpeed(UNINITIALISED_SPEED)
            .range(Setting.DEFAULT_PATHFINDING_RANGE.asFloat()).debug(Setting.DEBUG_PATHFINDING.asBoolean())
            .defaultAttackStrategy((attacker, target) -> {
                NMS.attack(attacker, target);
                return false;
            }).attackRange(Setting.NPC_ATTACK_DISTANCE.asDouble())
            .updatePathRate(Setting.DEFAULT_PATHFINDER_UPDATE_PATH_RATE.asTicks())
            .distanceMargin(Setting.DEFAULT_DISTANCE_MARGIN.asDouble())
            .pathDistanceMargin(Setting.DEFAULT_PATH_DISTANCE_MARGIN.asDouble())
            .stationaryTicks(Setting.DEFAULT_STATIONARY_DURATION.asTicks()).stuckAction(TeleportStuckAction.INSTANCE)
            .examiner(new MinecraftBlockExaminer()).useNewPathfinder(Setting.USE_NEW_PATHFINDER.asBoolean())
            .straightLineTargetingDistance(Setting.DEFAULT_STRAIGHT_LINE_TARGETING_DISTANCE.asFloat())
            .destinationTeleportMargin(Setting.DEFAULT_DESTINATION_TELEPORT_MARGIN.asDouble());
    private PathStrategy executing;
    private int lastX, lastY, lastZ;
    private NavigatorParameters localParams = defaultParams;
    private final NPC npc;
    private boolean paused;
    private PacketRotationSession session;
    private int stationaryTicks;

    public CitizensNavigator(NPC npc) {
        this.npc = npc;
        if (npc.data().get(NPC.Metadata.DISABLE_DEFAULT_STUCK_ACTION,
                !Setting.DEFAULT_STUCK_ACTION.asString().contains("teleport"))) {
            defaultParams.stuckAction(null);
        }
        defaultParams.examiner(new SwimmingExaminer(npc));
    }

    @Override
    public void cancelNavigation() {
        stopNavigating(CancelReason.PLUGIN);
    }

    @Override
    public void cancelNavigation(CancelReason reason) {
        stopNavigating(reason);
    }

    @Override
    public boolean canNavigateTo(Location dest) {
        return canNavigateTo(dest, defaultParams.clone());
    }

    @Override
    public boolean canNavigateTo(Location dest, NavigatorParameters params) {
        if (defaultParams.useNewPathfinder()) {
            AStarPlanner planner = new AStarPlanner(params, npc.getStoredLocation(), dest);
            planner.tick(Setting.MAXIMUM_ASTAR_ITERATIONS.asInt(), Setting.MAXIMUM_ASTAR_ITERATIONS.asInt());
            return planner.plan != null;
        } else {
            MCNavigator nav = NMS.getTargetNavigator(npc.getEntity(), dest, params);
            return nav.getCancelReason() == null;
        }
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
        if (!isNavigating())
            return defaultParams;
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
        return executing != null && !isPaused();
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
        if (root.keyExists("destinationteleportmargin")) {
            defaultParams.destinationTeleportMargin(root.getDouble("destinationteleportmargin"));
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
        if (!isNavigating() || !npc.isSpawned() || isPaused())
            return;

        Location npcLoc = npc.getStoredLocation();
        Location targetLoc = getTargetAsLocation();

        if (!npcLoc.getWorld().equals(targetLoc.getWorld()) || localParams.range() < npcLoc.distance(targetLoc)) {
            stopNavigating(CancelReason.STUCK);
            return;
        }
        if (updateStationaryStatus())
            return;

        updatePathfindingRange();
        boolean finished = executing.update();
        if (!finished) {
            localParams.run();
        }
        if (localParams.lookAtFunction() != null) {
            if (session == null) {
                RotationTrait trait = npc.getOrAddTrait(RotationTrait.class);
                session = trait
                        .createPacketSession(trait.getGlobalParameters().clone().filter(p -> true).persist(true));
            }
            session.getSession().rotateToFace(localParams.lookAtFunction().apply(this));
        }
        if (localParams.destinationTeleportMargin() > 0
                && npcLoc.distance(targetLoc) <= localParams.destinationTeleportMargin()) {
            // TODO: easing?
            npc.teleport(targetLoc, TeleportCause.PLUGIN);
            finished = true;
        }
        if (!finished)
            return;

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
        if (defaultParams.stationaryTicks() != Setting.DEFAULT_STATIONARY_DURATION.asTicks()) {
            root.setInt("stationaryticks", defaultParams.stationaryTicks());
        } else {
            root.removeKey("stationaryticks");
        }
        if (defaultParams.destinationTeleportMargin() != Setting.DEFAULT_DESTINATION_TELEPORT_MARGIN.asDouble()) {
            root.setDouble("destinationteleportmargin", defaultParams.destinationTeleportMargin());
        } else {
            root.removeKey("destinationteleportmargin");
        }
        if (defaultParams.distanceMargin() != Setting.DEFAULT_DISTANCE_MARGIN.asDouble()) {
            root.setDouble("distancemargin", defaultParams.distanceMargin());
        } else {
            root.removeKey("distancemargin");
        }
        if (defaultParams.updatePathRate() != Setting.DEFAULT_PATHFINDER_UPDATE_PATH_RATE.asTicks()) {
            root.setInt("updatepathrate", defaultParams.updatePathRate());
        } else {
            root.removeKey("updatepathrate");
        }
        if (defaultParams.useNewPathfinder() != Setting.USE_NEW_PATHFINDER.asBoolean()) {
            root.setBoolean("usenewpathfinder", defaultParams.useNewPathfinder());
        } else {
            root.removeKey("usenewpathfinder");
        }
        root.setDouble("speedmodifier", defaultParams.speedModifier());
        root.setBoolean("avoidwater", defaultParams.avoidWater());
        root.setBoolean("usedefaultstuckaction", defaultParams.stuckAction() == TeleportStuckAction.INSTANCE);
    }

    @Override
    public void setPaused(boolean paused) {
        if (paused && isNavigating()) {
            NMS.cancelMoveDestination(npc.getEntity());
        }
        this.paused = paused;
    }

    @Override
    public void setStraightLineTarget(Entity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");

        if (target == null) {
            cancelNavigation();
            return;
        }
        setTarget(params -> {
            params.straightLineTargetingDistance(100000);
            return new MCTargetStrategy(npc, target, aggressive, params);
        });
    }

    @Override
    public void setStraightLineTarget(Location target) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");

        if (target == null) {
            cancelNavigation();
            return;
        }
        setTarget(params -> new StraightLineNavigationStrategy(npc, target.clone(), params));
    }

    @Override
    public void setTarget(Entity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");

        if (target == null) {
            cancelNavigation();
            return;
        }
        setTarget(params -> new MCTargetStrategy(npc, target, aggressive, params));
    }

    @Override
    public void setTarget(Function<NavigatorParameters, PathStrategy> strategy) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        switchParams();
        switchStrategyTo(strategy.apply(localParams));
    }

    @Override
    public void setTarget(Iterable<Vector> path) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (path == null || Iterables.size(path) == 0) {
            cancelNavigation();
            return;
        }
        setTarget(params -> {
            if (npc.isFlyable())
                return new FlyingAStarNavigationStrategy(npc, path, params);
            else if (params.useNewPathfinder() || !(npc.getEntity() instanceof LivingEntity))
                return new AStarNavigationStrategy(npc, path, params);
            else
                return new MCNavigationStrategy(npc, path, params);
        });
    }

    @Override
    public void setTarget(Location targetIn) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (targetIn == null) {
            cancelNavigation();
            return;
        }
        Location target = targetIn.clone();
        setTarget(params -> {
            if (npc.isFlyable())
                return new FlyingAStarNavigationStrategy(npc, target, params);
            else if (params.useNewPathfinder() || !(npc.getEntity() instanceof LivingEntity))
                return new AStarNavigationStrategy(npc, target, params);
            else
                return new MCNavigationStrategy(npc, target, params);
        });
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
            NMS.cancelMoveDestination(npc.getEntity());
        }
        if (!SUPPORT_CHUNK_TICKETS || !CitizensAPI.hasImplementation() || !CitizensAPI.getPlugin().isEnabled())
            return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                () -> updateTicket(isNavigating() ? executing.getTargetAsLocation() : null), 10);

        // Location loc = npc.getEntity().getLocation(STATIONARY_LOCATION);
        // NMS.look(npc.getEntity(), loc.getYaw(), 0);
    }

    private void stopNavigating(CancelReason reason) {
        if (!isNavigating())
            return;

        if (reason == CancelReason.STUCK && Messaging.isDebugging()) {
            Messaging.debug(npc, "navigation ended, stuck", executing);
        }
        if (session != null) {
            session.end();
            session = null;
        }
        Iterator<NavigatorCallback> itr = localParams.callbacks().iterator();
        List<NavigatorCallback> callbacks = new ArrayList<>();
        while (itr.hasNext()) {
            callbacks.add(itr.next());
            itr.remove();
        }
        for (NavigatorCallback callback : callbacks) {
            callback.onCompletion(reason);
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
        int fallDistance = npc.data().get(NPC.Metadata.PATHFINDER_FALL_DISTANCE,
                Setting.PATHFINDER_FALL_DISTANCE.asInt());
        if (fallDistance != -1) {
            localParams.examiner(new FallingExaminer(fallDistance));
        }
        if (npc.data().get(NPC.Metadata.PATHFINDER_OPEN_DOORS, Setting.NEW_PATHFINDER_OPENS_DOORS.asBoolean())) {
            localParams.examiner(new DoorExaminer());
        }
        if (Setting.NEW_PATHFINDER_CHECK_BOUNDING_BOXES.asBoolean()) {
            localParams.examiner(new BoundingBoxExaminer(npc.getEntity()));
        }
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        updatePathfindingRange();
        if (executing != null) {
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        }
        executing = newStrategy;
        stationaryTicks = 0;
        if (npc.isSpawned()) {
            NMS.updateNavigationWorld(npc.getEntity(), npc.getEntity().getWorld());
            updateTicket(executing.getTargetAsLocation());
        }
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    private void updateMountedStatus() {
        // TODO: this method seems to break assumptions: better to let the NPC pathfind for itself rather than
        // "commanding" the NPC below on the stack
        if (!isNavigating() || true)
            return;
        Entity vehicle = NMS.getVehicle(npc.getEntity());
        if (!(vehicle instanceof NPCHolder))
            return;

        NPC mount = ((NPCHolder) vehicle).getNPC();
        if (mount.getNavigator().isNavigating())
            return;
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

        Location current = npc.getEntity().getLocation();
        if (!SpigotUtil.checkYSafe(current.getY(), current.getWorld())) {
            stopNavigating(CancelReason.STUCK);
            return true;
        }
        if (lastX == current.getBlockX() && lastY == current.getBlockY() && lastZ == current.getBlockZ()) {
            if (++stationaryTicks >= localParams.stationaryTicks()) {
                stopNavigating(CancelReason.STUCK);
                return true;
            }
        } else {
            stationaryTicks = 0;
        }
        lastX = current.getBlockX();
        lastY = current.getBlockY();
        lastZ = current.getBlockZ();
        return false;
    }

    private void updateTicket(Location target) {
        if (!SUPPORT_CHUNK_TICKETS || !CitizensAPI.hasImplementation() || !CitizensAPI.getPlugin().isEnabled())
            return;

        if (target != null && activeTicket != null
                && new ChunkCoord(target.getChunk()).equals(new ChunkCoord(activeTicket.getChunk()))) {
            activeTicket = target.clone();
            return;
        }
        if (activeTicket != null) {
            try {
                activeTicket.getChunk().removePluginChunkTicket(CitizensAPI.getPlugin());
            } catch (NoSuchMethodError e) {
                SUPPORT_CHUNK_TICKETS = false;
                activeTicket = null;
            }
        }
        if (target == null) {
            activeTicket = null;
            return;
        }
        activeTicket = target.clone();
        try {
            activeTicket.getChunk().addPluginChunkTicket(CitizensAPI.getPlugin());
        } catch (NoSuchMethodError e) {
            SUPPORT_CHUNK_TICKETS = false;
            activeTicket = null;
        }
    }

    private static boolean SUPPORT_CHUNK_TICKETS = true;
    private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;
}
