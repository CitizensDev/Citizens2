package net.citizensnpcs.api.astar;

/**
 * The storage for an {@link AStarMachine}. Controls the <em>open</em> and <em>closed</em> sets.
 */
public interface AStarStorage {
    /**
     * <em>Close</em> a given {@link AStarNode}, moving it from the open set to the closed set.
     */
    void close(AStarNode node);

    /**
     * @return The {@link AStarNode} to examine next from the frontier
     */
    AStarNode getBestNode();

    /**
     * <em>Close</em> a given {@link AStarNode}, moving or adding it from the frontier to the open set.
     */
    void open(AStarNode node);

    /**
     * Returns the best node from the frontier and removes it.
     *
     * @return The {@link AStarNode} to examine next from the frontier
     */
    AStarNode removeBestNode();

    /**
     * Returns whether to examine a given {@link AStarNode}.
     */
    boolean shouldExamine(AStarNode neighbour);
}
