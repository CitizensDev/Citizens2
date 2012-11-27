package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public abstract class AbstractBlockSource implements BlockSource {
    @Override
    public int getBlockTypeIdAt(Vector pos) {
        return getBlockTypeIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    @Override
    public Material getMaterialAt(int x, int y, int z) {
        return Material.getMaterial(getBlockTypeIdAt(x, y, z));
    }

    @Override
    public Material getMaterialAt(Vector pos) {
        return getMaterialAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
}
