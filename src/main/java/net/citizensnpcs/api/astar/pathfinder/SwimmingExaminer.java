package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.entity.WaterMob;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

public class SwimmingExaminer implements BlockExaminer {
    private boolean canSwimInLava;
    private final NPC npc;

    public SwimmingExaminer(NPC npc) {
        this.npc = npc;
    }

    public boolean canSwimInLava() {
        return canSwimInLava;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        if (SpigotUtil.isUsing1_13API() && npc.getEntity() instanceof WaterMob) {
            Vector vector = point.getVector();
            if (!MinecraftBlockExaminer.isLiquidOrInLiquid(
                    source.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()))) {
                return 0.5F;
            }
        }
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector vector = point.getVector();
        if (!MinecraftBlockExaminer.isLiquidOrInLiquid(
                source.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()))) {
            return PassableState.IGNORE;
        }
        if (SpigotUtil.isUsing1_13API() && npc.getEntity() instanceof WaterMob) {
            return PassableState.PASSABLE;
        }
        Material above = source.getMaterialAt(vector.add(new Vector(0, 1, 0)));
        PassableState canSwim = isSwimmableLiquid(above) || MinecraftBlockExaminer.canStandIn(above)
                ? PassableState.PASSABLE
                : PassableState.UNPASSABLE;
        if (point.getParentPoint() == null) {
            return canSwim;
        }
        if (vector.getBlockY() < point.getParentPoint().getVector().getBlockY()) {
            if (!isSwimmableLiquid(source.getMaterialAt(point.getParentPoint().getVector()))) {
                return canSwim;
            }
            return isSwimming() ? PassableState.UNPASSABLE : PassableState.PASSABLE;
        }
        return canSwim;
    }

    private boolean isSwimmableLiquid(Material material) {
        if (material == Material.LAVA
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_LAVA")))
            return canSwimInLava();
        return material == Material.WATER
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_WATER"));
    }

    public boolean isSwimming() {
        return npc.data().get(NPC.SWIMMING_METADATA, true);
    }

    public void setCanSwimInLava(boolean canSwimInLava) {
        this.canSwimInLava = canSwimInLava;
    }
}
