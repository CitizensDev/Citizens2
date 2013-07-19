package net.citizensnpcs.api.astar.pathfinder;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class MinecraftBlockExaminer implements BlockExaminer {
    private boolean contains(Material[] search, Material... find) {
        for (Material haystack : search) {
            for (Material needle : find)
                if (haystack == needle)
                    return true;
        }
        return false;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (above == Material.WEB || in == Material.WEB)
            return 1F;
        if (below == Material.SOUL_SAND || below == Material.ICE)
            return 1F;
        if (isLiquid(above, below, in))
            return 0.5F;
        return 0.5F; // TODO: add light level-specific costs
    }

    private boolean isLiquid(Material... materials) {
        return contains(materials, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
    }

    @Override
    public boolean isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (!below.isBlock() || !canStandOn(below)) {
            return false;
        }
        if (!canStandIn(above) || !canStandIn(in)) {
            return false;
        }
        return true;
    }

    private static final Vector DOWN = new Vector(0, -1, 0);

    private static final Set<Material> PASSABLE = EnumSet.of(Material.AIR, Material.DEAD_BUSH, Material.DETECTOR_RAIL,
            Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.FENCE_GATE,
            Material.ITEM_FRAME, Material.LADDER, Material.LEVER, Material.LONG_GRASS, Material.MELON_STEM,
            Material.NETHER_FENCE, Material.PUMPKIN_STEM, Material.POWERED_RAIL, Material.RAILS, Material.RED_ROSE,
            Material.RED_MUSHROOM, Material.REDSTONE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_OFF,
            Material.REDSTONE_WIRE, Material.SIGN, Material.SIGN_POST, Material.SNOW, Material.STRING,
            Material.STONE_BUTTON, Material.SUGAR_CANE_BLOCK, Material.TRIPWIRE, Material.VINE, Material.WALL_SIGN,
            Material.WHEAT, Material.WATER, Material.WEB, Material.WOOD_BUTTON, Material.WOODEN_DOOR,
            Material.STATIONARY_WATER);

    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA,
            Material.CACTUS);
    private static final Vector UP = new Vector(0, 1, 0);

    public static boolean canStandIn(Material mat) {
        return PASSABLE.contains(mat);
    }

    public static boolean canStandOn(Block block) {
        Block up = block.getRelative(BlockFace.UP);
        return canStandOn(block.getType()) && canStandIn(up.getType())
                && canStandIn(up.getRelative(BlockFace.UP).getType());
    }

    public static boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat) && !PASSABLE.contains(mat);
    }
}
