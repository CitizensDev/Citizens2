package net.citizensnpcs.npc.ai;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.NMSReflection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CitizensNavigator implements Navigator {
    private PathStrategy executing;
    private final CitizensNPC npc;
    private float pathfindingRange = Setting.DEFAULT_PATHFINDING_RANGE.asFloat();
    private float speed = -1;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void cancelNavigation() {
        if (executing != null) {
            Bukkit.getPluginManager().callEvent(new NavigationCancelEvent(this));
        }
        executing = null;
    }

    @Override
    public EntityTarget getEntityTarget() {
        return executing instanceof EntityTarget ? (EntityTarget) executing : null;
    }

    @Override
    public float getPathfindingRange() {
        return pathfindingRange;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public Location getTargetAsLocation() {
        return executing.getTargetAsLocation();
    }

    @Override
    public TargetType getTargetType() {
        return executing.getTargetType();
    }

    @Override
    public boolean isNavigating() {
        return executing != null;
    }

    public void load(DataKey root) {
        speed = (float) root.getDouble("speed", speed);
        pathfindingRange = (float) root.getDouble("pathfinding-range", pathfindingRange);
    }

    public void onSpawn() {
        if (speed == -1)
            this.speed = NMSReflection.getSpeedFor(npc.getHandle());
        updatePathfindingRange();
    }

    public void save(DataKey root) {
        root.setDouble("speed", speed);
        root.setDouble("pathfinding-range", pathfindingRange);
    }

    @Override
    public void setPathfindingRange(float newRange) {
        pathfindingRange = newRange;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        PathStrategy newStrategy = new MCTargetStrategy(npc, target, aggressive, speed);
        switchStrategyTo(newStrategy);
    }

    @Override
    public void setTarget(Location target) {
        if (!npc.isSpawned())
            throw new IllegalStateException("npc is not spawned");
        PathStrategy newStrategy = new MCNavigationStrategy(npc, target, speed);
        switchStrategyTo(newStrategy);
    }

    private void switchStrategyTo(PathStrategy newStrategy) {
        if (executing != null)
            Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
        executing = newStrategy;
        Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
    }

    public void update() {
        if (executing == null || !npc.isSpawned())
            return;
        boolean finished = executing.update();
        if (finished) {
            Bukkit.getPluginManager().callEvent(new NavigationCompleteEvent(this));
            executing = null;
        }
    }

    private void updatePathfindingRange() {
        NMSReflection.updatePathfindingRange(npc, pathfindingRange);
    }
}
