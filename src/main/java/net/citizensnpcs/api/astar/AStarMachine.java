package net.citizensnpcs.api.astar;

public class AStarMachine {
    private final AStarStorage storage;

    private AStarMachine(AStarStorage storage) {
        this.storage = storage;
    }

    private void f(AStarGoal goal, AStarNode node, AStarNode neighbour) {
        float g = node.g + goal.g(node, neighbour); // estimate the cost from
                                                    // the start additively
        float h = goal.h(neighbour);

        neighbour.f = g + h;
        neighbour.g = g;
        neighbour.h = h;
    }

    public Plan run(AStarGoal goal, AStarNode start) {
        storage.beginNewGoal();
        storage.open(start);
        start.f = goal.getInitialCost(start);
        AStarNode node;
        int iterations = 0;
        while (true) {
            node = storage.removeBestNode();
            if (node == null) {
                System.err.println("Expanded " + iterations);
                return null;
            }
            if (goal.isFinished(node)) {
                System.err.println("Expanded " + iterations);
                return node.buildPlan();
            }
            storage.close(node);
            for (AStarNode neighbour : node.getNeighbours()) {
                f(goal, node, neighbour);
                if (!storage.shouldExamine(neighbour))
                    continue;
                storage.open(neighbour);
                neighbour.parent = node;
                iterations++;
            }
        }
    }

    public static AStarMachine createWithDefaultStorage() {
        return createWithStorage(new SimpleAStarStorage());
    }

    public static AStarMachine createWithStorage(AStarStorage storage) {
        return new AStarMachine(storage);
    }
}
