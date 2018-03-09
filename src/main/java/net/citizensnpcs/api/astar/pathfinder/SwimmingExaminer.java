package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

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
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Material in = source.getMaterialAt(point.getVector());
        if (!MinecraftBlockExaminer.isLiquid(in)) {
            return PassableState.IGNORE;
        }
        Material above = source.getMaterialAt(point.getVector().add(new Vector(0, 1, 0)));
        PassableState canSwim = isSwimmableLiquid(above) || MinecraftBlockExaminer.canStandIn(above)
                ? PassableState.PASSABLE : PassableState.UNPASSABLE;
        if (point.getParentPoint() == null) {
            return canSwim;
        }
        if (point.getVector().getBlockY() < point.getParentPoint().getVector().getBlockY()) {
            if (!isSwimmableLiquid(source.getMaterialAt(point.getParentPoint().getVector()))) {
                return canSwim;
            }
            return isSwimming() ? PassableState.UNPASSABLE : PassableState.PASSABLE;
        }
        return canSwim;
    }

    private boolean isSwimmableLiquid(Material material) {
        if (material == Material.LAVA || material == Material.STATIONARY_LAVA)
            return canSwimInLava();
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    public boolean isSwimming() {
        return npc.data().get(NPC.SWIMMING_METADATA, true);
    }

    public void setCanSwimInLava(boolean canSwimInLava) {
        this.canSwimInLava = canSwimInLava;
    }
}
