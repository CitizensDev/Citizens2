package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;

import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.NeighbourGeneratorBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;

public class FallMovementExaminer implements NeighbourGeneratorBlockExaminer {
    private final int maxFallDistance;

    public FallMovementExaminer(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        Vector location = point.getVector();
        List<PathPoint> neighbours = ((VectorNode) point).getNeighbours(source, point);
        if (location.getBlockY() <= 1 || point.getGoal().getBlockY() >= location.getBlockY())
            return neighbours;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;
                Vector mod = location.clone().add(new Vector(x, 0, z));
                if (MinecraftBlockExaminer
                        .canStandOn(source.getMaterialAt(mod.getBlockX(), mod.getBlockY() - 1, mod.getBlockZ())))
                    continue;
                for (int dy = 2; dy < maxFallDistance; dy++) {
                    if (mod.getBlockY() - dy <= 2 || !MinecraftBlockExaminer
                            .canStandIn(source.getMaterialAt(mod.getBlockX(), mod.getBlockY() - dy, mod.getBlockZ())))
                        break;
                    if (MinecraftBlockExaminer.canStandOn(
                            source.getMaterialAt(mod.getBlockX(), mod.getBlockY() - dy - 1, mod.getBlockZ()))) {
                        PathPoint fall = point.createAtOffset(mod.clone().setY(mod.getBlockY() - dy));
                        fall.setPathVectors(ImmutableList.of(mod, fall.getVector()));
                        neighbours.add(fall);
                        break;
                    }
                }
            }
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        return PassableState.IGNORE;
    }
}
