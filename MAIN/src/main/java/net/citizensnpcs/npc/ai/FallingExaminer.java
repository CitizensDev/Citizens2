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
        if (!SpigotUtil.checkYSafe(pos.getBlockY() - 1, source.getWorld()))
            return PassableState.IGNORE;

        if (fall.containsKey(point))
            return PassableState.PASSABLE;

        Vector ppos = point.getParentPoint().getVector();
        if (!MinecraftBlockExaminer
                .canStandOn(source.getBlockAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ()))) {
            Integer dist = fall.get(point.getParentPoint());
            if (dist == null && mc.isPassable(source, point.getParentPoint()) == PassableState.PASSABLE) {
                // start a fall
                fall.put(point, 0);
                return PassableState.PASSABLE;
            } else if (dist != null && dist < maxFallDistance && pos.getBlockY() < ppos.getBlockY()
                    && pos.getBlockX() == ppos.getBlockX() && pos.getBlockZ() == ppos.getBlockZ()
                    && MinecraftBlockExaminer.canStandIn(source.getBlockAt(pos))) {
                fall.put(point, dist + 1);
                return PassableState.PASSABLE;
            }
        }
        return PassableState.IGNORE;
    }
}
