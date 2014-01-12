package net.citizensnpcs.api.astar.pathfinder;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class MinecraftBlockExaminer implements BlockExaminer {
    private boolean checkGoal(PathPoint point, Material in) {
        if (point.getGoal().equals(point.getVector())) {
            if (!canStandIn(in) && point.getParentPoint() != null) {
                point.setVector(point.getParentPoint().getVector());
            }
            return true;
        }
        return false;
    }

    private boolean checkLadders(BlockSource source, PathPoint point, Material above, Material below, Material in) {
        if (above == Material.LADDER && in == Material.LADDER) {
            return true;
        }
        if (below == Material.LADDER)
            return true;
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

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (!below.isBlock() || !canStandOn(below)) {
            return PassableState.UNPASSABLE;
        }
        if ((!canStandIn(above) || !canStandIn(in)) && !checkGoal(point, in) /*&& !checkLadders(source, point, above, below, in)*/) {
            return PassableState.UNPASSABLE;
        }/*
         if (in == Material.LADDER) {
            point.addCallback(new PathCallback() {
                boolean added = false;

                @Override
                public void run(final NPC npc, Block point, double radius) {
                    if (added || npc.data().<Boolean> get("running-ladder", false)) {
                        added = true;
                        return;
                    }
                    npc.getNavigator().getLocalParameters().addRunCallback(new Runnable() {
                        Location dummy = new Location(null, 0, 0, 0);

                        @Override
                        public void run() {
                            System.err.println('d');
                            if (npc.getEntity().getLocation(dummy).getBlock().getType() == Material.LADDER) {
                                npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.5));
                            } else {
                                npc.getNavigator().getLocalParameters().removeRunCallback(this);
                            }
                        }
                    });
                    added = true;
                    npc.data().set("running-ladder", true);
                }
            });
         }*/
        return PassableState.PASSABLE;
    }

    public static boolean canStandIn(Material... mat) {
        return PASSABLE.containsAll(Arrays.asList(mat));
    }

    public static boolean canStandOn(Block block) {
        Block up = block.getRelative(BlockFace.UP);
        return canStandOn(block.getType()) && canStandIn(up.getType())
                && canStandIn(up.getRelative(BlockFace.UP).getType());
    }

    public static boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat) && !PASSABLE.contains(mat);
    }

    private static boolean contains(Material[] search, Material... find) {
        for (Material haystack : search) {
            for (Material needle : find) {
                if (haystack == needle)
                    return true;
            }
        }
        return false;
    }

    public static Location findValidLocation(Location location, int radius) {
        Block base = location.getBlock();
        if (canStandOn(base))
            return location;
        for (int y = 0; y < radius; y++) {
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    Block relative = base.getRelative(x, y, z);
                    if (canStandOn(relative)) {
                        return relative.getLocation();
                    }
                }
            }
        }
        return location;
    }

    public static boolean isLiquid(Material... materials) {
        return contains(materials, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
    }

    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Set<Material> PASSABLE = EnumSet.of(Material.AIR, Material.DEAD_BUSH, Material.DETECTOR_RAIL,
            Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.FENCE_GATE,
            Material.ITEM_FRAME, Material.LEVER, Material.LONG_GRASS, Material.CARPET, Material.MELON_STEM,
            Material.NETHER_FENCE, Material.PUMPKIN_STEM, Material.POWERED_RAIL, Material.RAILS, Material.RED_ROSE,
            Material.RED_MUSHROOM, Material.REDSTONE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_OFF,
            Material.REDSTONE_WIRE, Material.SIGN, Material.SIGN_POST, Material.SNOW, Material.DOUBLE_PLANT,
            Material.STRING, Material.STONE_BUTTON, Material.SUGAR_CANE_BLOCK, Material.TRIPWIRE, Material.VINE,
            Material.WALL_SIGN, Material.WHEAT, Material.WATER, Material.WEB, Material.WOOD_BUTTON,
            Material.WOODEN_DOOR, Material.STATIONARY_WATER);
    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA,
            Material.CACTUS);
    private static final Vector UP = new Vector(0, 1, 0);
}
