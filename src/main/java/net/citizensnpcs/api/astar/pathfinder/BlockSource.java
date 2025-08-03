package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.util.BoundingBox;

public abstract class BlockSource {
    public Block getBlockAt(int x, int y, int z) {
        return getWorld().getBlockAt(x, y, z);
    }

    public Block getBlockAt(Vector position) {
        return getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    public abstract BoundingBox getCollisionBox(int x, int y, int z);

    public BoundingBox getCollisionBox(Vector pos) {
        return getCollisionBox(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public abstract Material getMaterialAt(int x, int y, int z);

    public Material getMaterialAt(Vector pos) {
        return getMaterialAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public abstract World getWorld();
}
