package net.citizensnpcs.npc.ai;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.NeighbourGeneratorBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;

public class FallingExaminer implements NeighbourGeneratorBlockExaminer {
    private final Map<PathPoint, Integer> fallen = Maps.newHashMap();
    private final int maxFallDistance;

    public FallingExaminer(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        List<PathPoint> neighbours = ((VectorNode) point).getNeighbours(source, point);
        if (pos.getBlockY() <= 1)
            return neighbours;

        Material above = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ());
        Material below = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());
        Material in = source.getMaterialAt(pos);
        if (!MinecraftBlockExaminer.canStandOn(below) && MinecraftBlockExaminer.canStandIn(above, in)) {
            Integer dist = fallen.get(point);
            if (dist == null) {
                neighbours.add(point);
                fallen.put(point, dist = 0);
            } else if (dist < maxFallDistance) {
                fallen.put(point, dist + 1);
                neighbours.add(point.createAtOffset(new Vector(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ())));
            }
        } else {
            fallen.remove(point);
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        if (fallen.containsKey(point)) {
            return PassableState.PASSABLE;
        }
        return PassableState.IGNORE;
    }
}
