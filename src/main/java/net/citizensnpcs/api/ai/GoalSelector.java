package net.citizensnpcs.api.ai;

public interface GoalSelector {
    /**
     * Stops executing any currently running {@link Goal}s and allows other
     * goals to be selected for execution.
     */
    void finish();

    /**
     * Calls {@link #finish()} and removes the {@link Goal} from the list of
     * possible goals to execute.
     */
    void finishAndRemove();

    /**
     * Replaces the execution of any running {@link Goal}s with the specified
     * goal.
     * 
     * @param goal
     *            The new goal for execution
     */
    void select(Goal goal);

    /**
     * Adds the provided {@link Goal}s to the execution list. These goals will
     * be executed along with any previously running goals.
     * 
     * @param goals
     *            The additional goals
     */
    void selectAdditional(Goal... goals);
}