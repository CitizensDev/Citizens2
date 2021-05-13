package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;
import java.util.ListIterator;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

public interface PathPoint {
    /**
     * Adds a path callback that will be executed if this path point is executed.
     */
    void addCallback(PathCallback callback);

    /**
     * Returns a new PathPoint at a given Vector.
     */
    PathPoint createAtOffset(Vector vector);

    /**
     * Gets the destination Vector
     */
    Vector getGoal();

    /**
     * Gets the parent PathPoint
     */
    PathPoint getParentPoint();

    /**
     * Gets the list of manual path vectors
     *
     * @see #setPathVectors(List)
     */
    List<Vector> getPathVectors();

    /**
     * Gets the vector represented by this point
     */
    Vector getVector();

    /**
     * Sets the path vectors that will be used at pathfinding time. For example, setting a list of vectors to path
     * through in order to reach this pathpoint.
     */
    void setPathVectors(List<Vector> vectors);

    /**
     * Sets the vector location of this point
     */
    void setVector(Vector vector);

    public static interface PathCallback {
        /**
         * A callback that is run every tick while the path is being executed.
         *
         * @param npc
         * @param point
         *            The current target path
         * @param path
         *            The future path destinations as blocks
         */
        void run(NPC npc, Block point, ListIterator<Block> path);
    }
}
