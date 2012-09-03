package net.citizensnpcs.npc.ai;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CitizensNavigator implements Navigator {
    private final NavigatorParameters defaultParams = new NavigatorParameters().speed(UNINITIALISED_SPEED)
            .range(Setting.DEFAULT_PATHFINDING_RANGE.asFloat())
            .stationaryTicks(Setting.DEFAULT_STATIONARY_TICKS.asInt());
    private PathStrategy executing;
    private NavigatorParameters localParams = defaultParams;
    private final CitizensNPC npc;
    private int stationaryTicks;

    private boolean updatedAvoidWater = false;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void cancelNavigation() {
        if (isNavigating())
            Bukkit.getPluginManager().callEvent(new NavigationCancelEvent(this, CancelReason.PLUGIN));
        stopNavigating();
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
            throw new IllegalStateException("not navigating");
        return localParams;
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
        defaultParams.speed((float) root.getDouble("speed", UNINITIALISED_SPEED));
        defaultParams.range((float) root.getDouble("pathfindingrange",
                Setting.DEFAULT_PATHFINDING_RANGE.asFloat()));
        defaultParams
                .stationaryTicks(root.getInt("stationaryticks", Setting.DEFAULT_STATIONARY_TICKS.asInt()));
        defaultParams.speedModifier((float) root.getDouble("speedmodifier", 1F));
        if (root.keyExists("avoidwater"))
            defaultParams.avoidWater(root.getBoolean("avoidwater"));
    }

    public void onSpawn() {
        if (defaultParams.speed() == UNINITIALISED_SPEED)
            defaultParams.speed(NMS.getSpeedFor(npc.getHandle()));
        updatePathfindingRange();
        if (!updatedAvoidWater) {
            boolean defaultAvoidWater = npc.getHandle().getNavigation().a();
            defaultParams.avoidWater(defaultAvoidWater);
            updatedAvoidWater = true;
        }
    }

    public void save(DataKey root) {
        root.setDouble("speed", defaultParams.baseSpeed());
        root.setDouble("pathfindingrange", defaultParams.range());
        root.setInt("stationaryticks", defaultParams.stationaryTicks());
        root.setDouble("speedmodifier", defaultParams.speedModifier());
        root.setBoolean("avoidwater", defaultParams.avoidWater());
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
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
    public void setTarget(Location target) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
        localParams = defaultParams.clone();
        PathStrategy newStrategy = new MCNavigationStrategy(npc, target, localParams);
        switchStrategyTo(newStrategy);
    }

    private void stopNavigating() {
        if (executing != null)
            executing.stop();
        executing = null;
        localParams = defaultParams;
        stationaryTicks = 0;
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (executing != null)
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        executing = newStrategy;
        stationaryTicks = 0;
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    public void update() {
        if (!isNavigating() || !npc.isSpawned() || updateStationaryStatus())
            return;
        boolean finished = executing.update();
        if (finished) {
            Bukkit.getPluginManager().callEvent(new NavigationCompleteEvent(this));
            stopNavigating();
        }
    }

    private void updatePathfindingRange() {
        NMS.updatePathfindingRange(npc, localParams.range());
    }

    private boolean updateStationaryStatus() {
        if (localParams.stationaryTicks() < 0)
            return false;
        EntityLiving handle = npc.getHandle();
        if ((int) handle.lastX == (int) handle.locX && (int) handle.lastY == (int) handle.locY
                && (int) handle.lastZ == (int) handle.locZ) {
            if (++stationaryTicks >= localParams.stationaryTicks()) {
                StuckAction action = localParams.stuckAction();
                if (action != null)
                    action.run(npc, this);
                Bukkit.getPluginManager().callEvent(new NavigationCancelEvent(this, CancelReason.STUCK));
                stopNavigating();
                return true;
            }
        } else
            stationaryTicks = 0;
        return false;
    }

    private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;
}
