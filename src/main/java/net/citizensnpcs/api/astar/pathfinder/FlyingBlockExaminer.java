package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public class FlyingBlockExaminer implements BlockExaminer {
    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material in = source.getMaterialAt(pos);
        if (above == Material.WEB || in == Material.WEB) {
            return 1F;
        }
        return 0.5F;
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
}