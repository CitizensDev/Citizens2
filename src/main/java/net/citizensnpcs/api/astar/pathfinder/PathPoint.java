package net.citizensnpcs.api.astar.pathfinder;

import java.util.ListIterator;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public interface PathPoint {
    void addCallback(PathCallback callback);

    Vector getGoal();

    PathPoint getParentPoint();

    Vector getVector();

    void setVector(Vector vector);

    public static interface PathCallback {
        void run(NPC npc, Block point, ListIterator<Block> path);
    }
}
