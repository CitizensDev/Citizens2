package net.citizensnpcs.api.astar;

public abstract class UnboundedAStarGoal<T extends AStarNode> implements AStarGoal<T> {
    @Override
    public float getInitialCost(T node) {
        return 0;
    }

    @Override
    public boolean isFinished(T node) {
        return false;
    }
}
