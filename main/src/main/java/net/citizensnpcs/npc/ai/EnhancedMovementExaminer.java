package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.NeighbourGeneratorBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;

public class EnhancedMovementExaminer implements NeighbourGeneratorBlockExaminer {
    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        Vector location = point.getVector();
        List<PathPoint> neighbours = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (x != 0 && z != 0)
                        continue;
                    Vector mod = location.clone().add(new Vector(x, y, z));
                    if (mod.equals(location))
                        continue;
                    neighbours.add(point.createAtOffset(mod));
                }
            }
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        return null;
    }
}
