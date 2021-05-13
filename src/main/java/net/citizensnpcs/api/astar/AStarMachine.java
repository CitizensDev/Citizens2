package net.citizensnpcs.api.astar;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

public class AStarMachine<N extends AStarNode, P extends Plan> {
    private Supplier<AStarStorage> storageSupplier;

    private AStarMachine(Supplier<AStarStorage> storage) {
        this.storageSupplier = storage;
    }

    private void f(AStarGoal<N> goal, N node, N neighbour) {
        float g = node.g + goal.g(node, neighbour); // calculate the cost from the start additively
        float h = goal.h(neighbour);

        neighbour.g = g;
        neighbour.h = h;
    }

    private AStarStorage getInitialisedStorage(AStarGoal<N> goal, N start) {
        AStarStorage storage = storageSupplier.get();
        storage.open(start);
        start.g = goal.getInitialCost(start);
        start.h = 0;
        return storage;
    }

    /**
     * Creates an {@link AStarState} that can be reused across multiple invocations of {{@link #run(AStarState, int)}.
     *
     * @see #run(AStarState, int)
     * @param goal
     *            The {@link AStarGoal} state
     * @param start
     *            The starting {@link AStarNode}
     * @return The created state
     */
    public AStarState getStateFor(AStarGoal<N> goal, N start) {
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
    public P run(AStarState state) {
        return run(state, -1);
    }

    /**
     * Runs the machine using the given {@link AStarState}'s {@link AStarStorage}. Can be used to provide a continuation
     * style usage of the A* algorithm.
     *
     * @param state
     *            The state to use
     * @param maxIterations
     *            The maximum number of iterations
     * @return The generated {@link Plan}, or <code>null</code> if not found
     */
    public P run(AStarState state, int maxIterations) {
        return run(state.storage, state.goal, state.start, maxIterations);
    }

    @SuppressWarnings("unchecked")
    private P run(AStarStorage storage, AStarGoal<N> goal, N start, int maxIterations) {
        Preconditions.checkNotNull(goal);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(storage);
        N node;
        int iterations = 0;
        while (true) {
            node = (N) storage.removeBestNode();
            if (node == null) {
                return null;
            }
            if (goal.isFinished(node)) {
                return (P) node.buildPlan();
            }
            storage.close(node);
            for (AStarNode neighbour : node.getNeighbours()) {
                f(goal, node, (N) neighbour);
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
    public P runFully(AStarGoal<N> goal, N start) {
        return runFully(goal, start, -1);
    }

    /**
     * Runs the machine fully until the iteration limit has been exceeded. This will use the supplied goal and start to
     * generate neighbours until the goal state has been reached using the A* algorithm.
     *
     * @param goal
     *            The {@link AStarGoal} state
     * @param start
     *            The starting {@link AStarNode}
     * @param iterations
     *            The number of iterations to run the machine for
     * @return The generated {@link Plan}, or <code>null</code> if it was not found
     */
    public P runFully(AStarGoal<N> goal, N start, int iterations) {
        return run(getInitialisedStorage(goal, start), goal, start, iterations);
    }

    /**
     * Sets the {@link Supplier} to use to generate instances of {@link AStarStorage} for use while searching.
     *
     * @param newSupplier
     *            The new supplier to use
     */
    public void setStorageSupplier(Supplier<AStarStorage> newSupplier) {
        storageSupplier = newSupplier;
    }

    public class AStarState {
        private final AStarGoal<N> goal;
        private final N start;
        private final AStarStorage storage;

        private AStarState(AStarGoal<N> goal, N start, AStarStorage storage) {
            this.goal = goal;
            this.start = start;
            this.storage = storage;
        }

        @SuppressWarnings("unchecked")
        public N getBestNode() {
            return (N) storage.getBestNode();
        }

        public boolean isEmpty() {
            return storage.getBestNode() == null;
        }
    }

    /**
     * Creates an AStarMachine using {@link SimpleAStarStorage} as the storage backend.
     *
     * @return The created instance
     */
    public static <N extends AStarNode, P extends Plan> AStarMachine<N, P> createWithDefaultStorage() {
        return createWithStorage(SimpleAStarStorage.FACTORY);
    }

    /**
     * Creates an AStarMachine that uses the given {@link Supplier <AStarStorage>} to create {@link AStarStorage}
     * instances.
     *
     * @param storageSupplier
     *            The storage supplier
     * @return The created instance
     */
    public static <N extends AStarNode, P extends Plan> AStarMachine<N, P> createWithStorage(
            Supplier<AStarStorage> storageSupplier) {
        return new AStarMachine<N, P>(storageSupplier);
    }
}
