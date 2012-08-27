package net.citizensnpcs.npc.ai;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.NMSReflection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CitizensNavigator implements Navigator {
    private final NavigatorParameters defaultParams = new NavigatorParameters().speed(UNINITIALISED_SPEED)
            .range(Setting.DEFAULT_PATHFINDING_RANGE.asFloat());
    private PathStrategy executing;
    private NavigatorParameters localParams = defaultParams;
    private final CitizensNPC npc;

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
        return localParams;
    }

    @Override
    public float getPathfindingRange() {
        return defaultParams.range();
    }

    @Override
    public float getSpeed() {
        if (defaultParams.speed() == UNINITIALISED_SPEED)
            throw new IllegalStateException("NPC has not been spawned");
        return defaultParams.speed();
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
        defaultParams.range((float) root.getDouble("pathfinding-range",
                Setting.DEFAULT_PATHFINDING_RANGE.asFloat()));
    }

    public void onSpawn() {
        if (defaultParams.speed() == UNINITIALISED_SPEED)
            defaultParams.speed(NMSReflection.getSpeedFor(npc.getHandle()));
        updatePathfindingRange();
    }

    public void save(DataKey root) {
        root.setDouble("speed", defaultParams.speed());
        root.setDouble("pathfinding-range", defaultParams.range());
    }

    @Override
    public void setPathfindingRange(float newRange) {
        defaultParams.range(newRange);
        if (isNavigating())
            localParams.range(newRange);
    }

    @Override
    public void setSpeed(float speed) {
        defaultParams.speed(speed);
        if (isNavigating())
            localParams.speed(speed);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        if (target == null) {
            cancelNavigation();
            return;
        }
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
        PathStrategy newStrategy = new MCNavigationStrategy(npc, target, localParams);
        switchStrategyTo(newStrategy);
    }

    private void stopNavigating() {
        executing = null;
        localParams = defaultParams;
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (executing != null)
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        executing = newStrategy;
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    public void update() {
        if (!isNavigating() || !npc.isSpawned())
            return;
        boolean finished = executing.update();
        if (finished) {
            Bukkit.getPluginManager().callEvent(new NavigationCompleteEvent(this));
            stopNavigating();
        }
    }

    private void updatePathfindingRange() {
        NMSReflection.updatePathfindingRange(npc, localParams.range());
    }

    private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;
}
