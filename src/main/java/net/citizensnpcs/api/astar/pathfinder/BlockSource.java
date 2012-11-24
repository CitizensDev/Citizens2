package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public interface BlockSource {
    int getBlockTypeIdAt(int x, int y, int z);

    int getBlockTypeIdAt(Vector pos);

    Material getMaterialAt(int x, int y, int z);

    Material getMaterialAt(Vector pos);
}