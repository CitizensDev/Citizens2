package net.citizensnpcs.npc.ai;

import java.util.Iterator;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CitizensNavigator implements Navigator, Runnable {
    private final NavigatorParameters defaultParams = new NavigatorParameters().baseSpeed(UNINITIALISED_SPEED)
            .range(Setting.DEFAULT_PATHFINDING_RANGE.asFloat())
            .stationaryTicks(Setting.DEFAULT_STATIONARY_TICKS.asInt()).stuckAction(TeleportStuckAction.INSTANCE)
            .examiner(new MinecraftBlockExaminer());
    private PathStrategy executing;
    private int lastX, lastY, lastZ;
    private NavigatorParameters localParams = defaultParams;
    private final NPC npc;
    private int stationaryTicks;

    public CitizensNavigator(NPC npc) {
        this.npc = npc;
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
        if (!isNavigating())
            return defaultParams;
        return localParams;
    }

    @Override
    public NPC getNPC() {
        return npc;
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

    public void load(DataKey root) {
        defaultParams.range((float) root.getDouble("pathfindingrange", Setting.DEFAULT_PATHFINDING_RANGE.asFloat()));
        defaultParams.stationaryTicks(root.getInt("stationaryticks", Setting.DEFAULT_STATIONARY_TICKS.asInt()));
        defaultParams.speedModifier((float) root.getDouble("speedmodifier", 1F));
        if (root.keyExists("avoidwater"))
            defaultParams.avoidWater(root.getBoolean("avoidwater"));
        if (!root.getBoolean("usedefaultstuckaction") && defaultParams.stuckAction() == TeleportStuckAction.INSTANCE)
            defaultParams.stuckAction(null);
    }

    public void onDespawn() {
        stopNavigating(CancelReason.NPC_DESPAWNED);
    }

    public void onSpawn() {
        if (defaultParams.baseSpeed() == UNINITIALISED_SPEED)
            defaultParams.baseSpeed(NMS.getSpeedFor(npc));
        updatePathfindingRange();
    }

    @Override
    public void run() {
        if (!isNavigating() || !npc.isSpawned())
            return;
        if (updateStationaryStatus())
            return;
        updatePathfindingRange();
        boolean finished = executing.update();
        if (!finished)
            return;
        if (executing.getCancelReason() != null) {
            stopNavigating(executing.getCancelReason());
        } else {
            NavigationCompleteEvent event = new NavigationCompleteEvent(this);
            PathStrategy old = executing;
            Bukkit.getPluginManager().callEvent(event);
            if (old == executing)
                stopNavigating(null);
        }
    }

    public void save(DataKey root) {
        root.setDouble("pathfindingrange", defaultParams.range());
        root.setInt("stationaryticks", defaultParams.stationaryTicks());
        root.setDouble("speedmodifier", defaultParams.speedModifier());
        root.setBoolean("avoidwater", defaultParams.avoidWater());
        root.setBoolean("usedefaultstuckaction", defaultParams.stuckAction() == TeleportStuckAction.INSTANCE);
    }

    @Override
    public void setTarget(Entity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
        localParams = defaultParams.clone();
        PathStrategy newStrategy = new MCTargetStrategy(npc, target, aggressive, localParams);
        switchStrategyTo(newStrategy);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        setTarget((Entity) target, aggressive);
    }

    @Override
    public void setTarget(Location target) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
        localParams = defaultParams.clone();
        PathStrategy newStrategy;
        if (Setting.USE_NEW_PATHFINDER.asBoolean() || localParams.useNewPathfinder()) {
            newStrategy = new AStarNavigationStrategy(npc, target, localParams);
        } else {
            newStrategy = new MCNavigationStrategy(npc, target, localParams);
        }
        switchStrategyTo(newStrategy);
    }

    private void stopNavigating() {
        if (executing != null)
            executing.stop();
        executing = null;
        localParams = defaultParams;
        stationaryTicks = 0;
        if (npc.isSpawned()) {
            Vector velocity = npc.getBukkitEntity().getVelocity();
            velocity.setX(0).setY(0).setZ(0);
            npc.getBukkitEntity().setVelocity(velocity);
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
        if (Messaging.isDebugging())
            Messaging.debug(npc.getId(), "cancelling with reason", reason);
        if (reason == null) {
            stopNavigating();
            return;
        }
        if (reason == CancelReason.STUCK && localParams.stuckAction() != null) {
            StuckAction action = localParams.stuckAction();
            boolean shouldContinue = action.run(npc, this);
            if (shouldContinue) {
                stationaryTicks = 0;
                executing.clearCancelReason();
                return;
            }
        }
        NavigationCancelEvent event = new NavigationCancelEvent(this, reason);
        PathStrategy old = executing;
        Bukkit.getPluginManager().callEvent(event);
        if (old == executing)
            stopNavigating();
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (Messaging.isDebugging())
            Messaging.debug(npc.getId(), "changing to new PathStrategy", newStrategy);
        if (executing != null)
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        executing = newStrategy;
        stationaryTicks = 0;
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    private void updatePathfindingRange() {
        NMS.updatePathfindingRange(npc, localParams.range());
    }

    private boolean updateStationaryStatus() {
        if (localParams.stationaryTicks() < 0)
            return false;
        Location current = npc.getBukkitEntity().getLocation(STATIONARY_LOCATION);
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

    private static final Location STATIONARY_LOCATION = new Location(null, 0, 0, 0);
    private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;
}
