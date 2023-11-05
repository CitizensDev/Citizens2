package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class MCNavigationStrategy extends AbstractPathStrategy {
    private final Entity entity;
    private final MCNavigator navigator;
    private final NavigatorParameters parameters;
    private final Location target;

    MCNavigationStrategy(NPC npc, Iterable<Vector> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector> list = Lists.newArrayList(path);
        target = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getWorld());
        parameters = params;
        entity = npc.getEntity();
        navigator = NMS.getTargetNavigator(npc.getEntity(), list, params);
    }

    MCNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        if (!MinecraftBlockExaminer.canStandIn(dest.getBlock())) {
            dest = MinecraftBlockExaminer.findValidLocationAbove(dest, 2);
        }
        target = Util.getCenterLocation(dest.getBlock());
        parameters = params;
        entity = npc.getEntity();
        navigator = NMS.getTargetNavigator(entity, target, params);
    }

    @Override
    public Location getCurrentDestination() {
        Location dest = NMS.getDestination(entity);
        return dest != null ? dest : target.clone();
    }

    @Override
    public Iterable<Vector> getPath() {
        return navigator.getPath();
    }

    @Override
    public Location getTargetAsLocation() {
        return target;
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.LOCATION;
    }

    @Override
    public void stop() {
        navigator.stop();
    }

    @Override
    public String toString() {
        return "MCNavigationStrategy [target=" + target + "]";
    }

    @Override
    public boolean update() {
        if (navigator.getCancelReason() != null) {
            setCancelReason(navigator.getCancelReason());
        }
        if (getCancelReason() != null)
            return true;
        boolean wasFinished = navigator.update();
        Location loc = entity.getLocation();
        double dX = target.getX() - loc.getX();
        double dZ = target.getZ() - loc.getZ();
        double dY = target.getY() - loc.getY();
        double xzDistance = Math.sqrt(dX * dX + dZ * dZ);
        if (Math.abs(dY) < 1 && xzDistance <= parameters.distanceMargin()) {
            stop();
            return true;
        }
        if (navigator.getCancelReason() != null) {
            setCancelReason(navigator.getCancelReason());
            return true;
        }
        return wasFinished;
    }

    public static interface MCNavigator {
        CancelReason getCancelReason();

        Iterable<Vector> getPath();

        void stop();

        boolean update();
    }
}
