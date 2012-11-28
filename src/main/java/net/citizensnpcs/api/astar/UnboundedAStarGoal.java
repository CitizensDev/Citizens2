package net.citizensnpcs.api.astar;

public abstract class UnboundedAStarGoal implements AStarGoal {
    @Override
    public float getInitialCost(AStarNode node) {
        return 0;
    }

    @Override
    public boolean isFinished(AStarNode node) {
        return false;
    }
}
