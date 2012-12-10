package net.citizensnpcs.api.astar;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AStarMachine {
    private Supplier<AStarStorage> storageSupplier;

    private AStarMachine(Supplier<AStarStorage> storage) {
        this.storageSupplier = storage;
    }

    private void f(AStarGoal goal, AStarNode node, AStarNode neighbour) {
        float g = node.g + goal.g(node, neighbour); // estimate the cost from
                                                    // the start additively
        float h = goal.h(neighbour);

        neighbour.f = g + h;
        neighbour.g = g;
        neighbour.h = h;
    }

    private AStarStorage getInitialisedStorage(AStarGoal goal, AStarNode start) {
        AStarStorage storage = storageSupplier.get();
        storage.open(start);
        start.f = goal.getInitialCost(start);
        return storage;
    }

    /**
     * Creates an {@link AStarState} that can be reused across multiple
     * invocations of {{@link #run(AStarState, int)}.
     * 
     * @see #run(AStarState, int)
     * @param goal
     *            The {@link AStarGoal} state
     * @param start
     *            The starting {@link AStarNode}
     * @return The created state
     */
    public AStarState getStateFor(AStarGoal<?> goal, AStarNode start) {
        return new AStarState(goal, start, getInitialisedStorage(goal, start));
    }

    /**
     * Runs the {@link AStarState} until a plan is found.
     * 
     * @see #run(AStarState)
     * @param state
     *            The state to use
     * @return The generated {@link Plan}, or <code>null</code>
     */
    public <T extends Plan> T run(AStarState state) {
        return run(state, -1);
    }

    /**
     * Runs the machine using the given {@link AStarState}'s
     * {@link AStarStorage}. Can be used to provide a continuation style usage
     * of the A* algorithm.
     * 
     * @param state
     *            The state to use
     * @param maxIterations
     *            The maximum number of iterations
     * @return The generated {@link Plan}, or <code>null</code> if not found
     */
    public <T extends Plan> T run(AStarState state, int maxIterations) {
        return run(state.storage, state.goal, state.start, maxIterations);
    }

    private <T extends Plan> T run(AStarStorage storage, AStarGoal goal, AStarNode start, int maxIterations) {
        Preconditions.checkNotNull(goal);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(storage);
        AStarNode node;
        int iterations = 0;
        while (true) {
            node = storage.removeBestNode();
            if (node == null) {
                return null;
            }
            if (goal.isFinished(node)) {
                return (T) node.buildPlan();
            }
            storage.close(node);
            for (AStarNode neighbour : node.getNeighbours()) {
                f(goal, node, neighbour);
                if (!storage.shouldExamine(neighbour))
                    continue;
                storage.open(neighbour);
                neighbour.parent = node;
            }
            if (maxIterations >= 0 && iterations++ >= maxIterations) {
                return null;
            }
        }
    }

    /**
     * Runs the machine until a plan is either found or cannot be generated.
     * 
     * @see #runFully(AStarGoal, AStarNode, int)
     */
    public <T extends Plan> T runFully(AStarGoal<?> goal, AStarNode start) {
        return runFully(goal, start, -1);
    }

    /**
     * Runs the machine fully until the iteration limit has been exceeded. This
     * will use the supplied goal and start to generate neighbours until the
     * goal state has been reached using the A* algorithm.
     * 
     * @param goal
     *            The {@link AStarGoal} state
     * @param start
     *            The starting {@link AStarNode}
     * @param iterations
     *            The number of iterations to run the machine for
     * @return The generated {@link Plan}, or <code>null</code> if it was not
     *         found
     */
    public <T extends Plan> T runFully(AStarGoal<?> goal, AStarNode start, int iterations) {
        return run(getInitialisedStorage(goal, start), goal, start, iterations);
    }

    /**
     * Sets the {@link Supplier} to use to generate instances of
     * {@link AStarStorage} for use while searching.
     * 
     * @param newSupplier
     *            The new supplier to use
     */
    public void setStorageSupplier(Supplier<AStarStorage> newSupplier) {
        storageSupplier = newSupplier;
    }

    public static class AStarState {
        private final AStarGoal<?> goal;
        private final AStarNode start;
        private final AStarStorage storage;

        private AStarState(AStarGoal<?> goal, AStarNode start, AStarStorage storage) {
            this.goal = goal;
            this.start = start;
            this.storage = storage;
        }

        public <T extends AStarNode> T getBestNode() {
            return (T) storage.getBestNode();
        }
    }

    /**
     * Creates an AStarMachine using {@link SimpleAStarStorage} as the storage
     * backend.
     * 
     * @return The created instance
     */
    public static AStarMachine createWithDefaultStorage() {
        return createWithStorage(SimpleAStarStorage.FACTORY);
    }

    /**
     * Creates an AStarMachine that uses the given {@link Supplier
     * <AStarStorage>} to create {@link AStarStorage} instances.
     * 
     * @param storageSupplier
     *            The storage supplier
     * @return The created instance
     */
    public static AStarMachine createWithStorage(Supplier<AStarStorage> storageSupplier) {
        return new AStarMachine(storageSupplier);
    }
}
