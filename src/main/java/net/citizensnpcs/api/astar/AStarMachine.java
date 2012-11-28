package net.citizensnpcs.api.astar;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

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

    public AStarState getStateFor(AStarGoal goal, AStarNode start) {
        return new AStarState(goal, start, getInitialisedStorage(goal, start));
    }

    public Plan run(AStarState state, int maxIterations) {
        return run(state.storage, state.goal, state.start, maxIterations);
    }

    private Plan run(AStarStorage storage, AStarGoal goal, AStarNode start, int maxIterations) {
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
                return node.buildPlan();
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
                System.err.println("Hit max iterations " + storage);
                return null;
            }
        }
    }

    public Plan runFully(AStarGoal goal, AStarNode start) {
        return runFully(goal, start, -1);
    }

    public Plan runFully(AStarGoal goal, AStarNode start, int iterations) {
        return run(getInitialisedStorage(goal, start), goal, start, iterations);
    }

    public void setStorageSupplier(Supplier<AStarStorage> newSupplier) {
        storageSupplier = newSupplier;
    }

    public static class AStarState {
        private final AStarGoal goal;
        private final AStarNode start;
        private final AStarStorage storage;

        private AStarState(AStarGoal goal, AStarNode start, AStarStorage storage) {
            this.goal = goal;
            this.start = start;
            this.storage = storage;
        }

        @SuppressWarnings("unchecked")
        public <T extends AStarNode> T getBestNode() {
            return (T) storage.getBestNode();
        }
    }

    public static AStarMachine createWithDefaultStorage() {
        return createWithStorage(new SimpleAStarStorage.Factory());
    }

    public static AStarMachine createWithStorage(Supplier<AStarStorage> storageSupplier) {
        return new AStarMachine(storageSupplier);
    }
}
