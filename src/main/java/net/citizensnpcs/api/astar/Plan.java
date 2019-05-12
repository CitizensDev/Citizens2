package net.citizensnpcs.api.astar;

/**
 * An abstract plan returned by the {@link AStarGoal} that should be run until completion.
 */
public interface Plan {
    boolean isComplete();

    /**
     * Updates the plan. Should be run ideally every tick.
     */
    void update(Agent agent);
}
