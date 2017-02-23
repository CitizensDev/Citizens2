package net.citizensnpcs.api.astar;

public interface AStarGoal<T extends AStarNode> {
    /**
     * Returns the cost of moving between the two supplied {@link AStarNode}s.
     * 
     * @param from
     *            The node to start from
     * @param to
     *            The end node
     * @return The cost
     */
    float g(T from, T to);

    /**
     * Returns the initial cost value when starting from the supplied {@link AStarNode}. This represents an initial
     * estimate for reaching the goal state from the start node.
     * 
     * @param node
     *            The start node
     * @return The initial cost
     */
    float getInitialCost(T node);

    /**
     * Returns the estimated heuristic cost of traversing from the supplied {@link AStarNode} to the goal.
     * 
     * @param from
     *            The start node
     * @return The heuristic cost
     */
    float h(T from);

    /**
     * Returns whether the supplied {@link AStarNode} represents the goal state for this <code>AStarGoal</code>. This
     * will halt execution of the calling {@link AStarMachine}.
     * 
     * @param node
     *            The node to check
     * @return Whether the node is the goal state
     */
    boolean isFinished(T node);
}
