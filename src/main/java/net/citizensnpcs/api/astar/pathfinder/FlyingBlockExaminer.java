package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.util.SpigotUtil;

public class FlyingBlockExaminer implements NeighbourGeneratorBlockExaminer {
    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material in = source.getMaterialAt(pos);
        if (above == WEB || in == WEB) {
            return 0.5F;
        }
        return 0F;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        List<PathPoint> neighbours = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    neighbours.add(point.createAtOffset(point.getVector().add(new Vector(x, y, z))));
                }
            }
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material in = source.getMaterialAt(pos);
        if (MinecraftBlockExaminer.isLiquid(above, in)) {
            return PassableState.UNPASSABLE;
        }
        return PassableState.fromBoolean(MinecraftBlockExaminer.canStandIn(above, in));
    }

    private static final Vector UP = new Vector(0, 1, 0);
    private static Material WEB = SpigotUtil.isUsing1_13API() ? Material.COBWEB : Material.valueOf("WEB");
}