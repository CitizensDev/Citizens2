package net.citizensnpcs.api.astar.pathfinder;


import org.bukkit.util.Vector;

public interface BlockExaminer {
    float getCost(BlockSource source, Vector pos);

    boolean isPassable(BlockSource source, Vector pos);
}