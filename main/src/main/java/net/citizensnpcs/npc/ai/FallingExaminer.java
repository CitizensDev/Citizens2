package net.citizensnpcs.npc.ai;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.NeighbourGeneratorBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;

public class FallingExaminer implements NeighbourGeneratorBlockExaminer {
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
            if (!point.data().has("fallen")) {
                neighbours.add(point);
                point.data().set("fallen", 0);
            } else if (point.data().<Integer> get("fallen") < maxFallDistance) {
                point.data().set("fallen", point.data().get("fallen", 0) + 1);
                neighbours.add(point.createAtOffset(new Vector(0, -1, 0)));
            }
        } else {
            if (point.data().has("fallen")) {
                point.data().remove("fallen");
            }
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        if (point.data().has("fallen")) {
            return PassableState.PASSABLE;
        }
        return PassableState.IGNORE;
    }
}
