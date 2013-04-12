package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public abstract class BlockSource {
    public abstract int getBlockTypeIdAt(int x, int y, int z);

    public Material getMaterialAt(int x, int y, int z) {
        return Material.getMaterial(getBlockTypeIdAt(x, y, z));
    }

    public Material getMaterialAt(Vector pos) {
        return getMaterialAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
}
