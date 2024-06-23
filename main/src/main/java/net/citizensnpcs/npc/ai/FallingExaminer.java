package net.citizensnpcs.npc.ai;

import java.util.Map;

import org.bukkit.util.Vector;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.util.SpigotUtil;

public class FallingExaminer implements BlockExaminer {

    private final Map<PathPoint, Integer> fall = Maps.newHashMap();
    private final int maxFallDistance;
    private final MinecraftBlockExaminer mc = new MinecraftBlockExaminer();

    public FallingExaminer(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return fall.containsKey(point) ? 0.25f : 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        PathPoint parentPoint = point.getParentPoint();
        Vector parentPos = parentPoint != null ? parentPoint.getVector() : null;

        // Ignore points above the previous point to fix "falling up"
        if (parentPos != null && pos.getBlockY() > parentPos.getBlockY()) {
            return PassableState.IGNORE;
        }

        if (!SpigotUtil.checkYSafe(pos.getBlockY() - 1, source.getWorld())) {
            return PassableState.IGNORE;
        }

        if (fall.containsKey(point)) {
            return PassableState.PASSABLE;
        }

        if (!MinecraftBlockExaminer
                .canStandOn(source.getBlockAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ()))) {
            if (parentPoint == null) {
                return PassableState.IGNORE;
            }
            Integer dist = fall.get(parentPoint);
            if (dist == null && mc.isPassable(source, parentPoint) == PassableState.PASSABLE) {
                // start a fall
                fall.put(point, 0);
                return PassableState.PASSABLE;
            } else if (dist != null && pos.getBlockY() < parentPoint.getVector().getBlockY()
                    && pos.getBlockX() == parentPoint.getVector().getBlockX()
                    && pos.getBlockZ() == parentPoint.getVector().getBlockZ() && dist < maxFallDistance) {
                fall.put(point, dist + 1);
                return PassableState.PASSABLE;
            }
        }
        return PassableState.IGNORE;
    }
}
