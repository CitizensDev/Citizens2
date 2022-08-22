package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

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
         * Run once the specificed point is reached.
         *
         * @param npc
         *            The NPC
         * @param point
         *            The point that was reached
         */
        default void onReached(NPC npc, Block point) {
        };

        /**
         * Run every tick when moving towards a specific block.
         *
         * @param npc
         *            The NPC
         * @param point
         *            The point
         * @param path
         *            The future path destinations as blocks
         * @param index
         *            The current path index
         */
        void run(NPC npc, Block current, List<Block> path, int index);
    }
}
