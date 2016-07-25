package net.citizensnpcs.api.astar.pathfinder;

import java.util.ListIterator;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

public interface PathPoint {
    void addCallback(PathCallback callback);

    PathPoint createAtOffset(Vector vector);

    Vector getGoal();

    PathPoint getParentPoint();

    Vector getVector();

    void setVector(Vector vector);

    public static interface PathCallback {
        void run(NPC npc, Block point, ListIterator<Block> path);
    }
}
