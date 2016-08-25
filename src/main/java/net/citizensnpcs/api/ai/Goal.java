package net.citizensnpcs.api.ai;

/**
 * Represents a Goal that can be added to a {@link GoalController}.
 *
 * A Goal represents an abstract node in a tree of events. It can be anything from attacking players to a villager. By
 * using the {@link GoalSelector} provided in {@link #shouldExecute(GoalSelector)} the Goal can traverse its tree of
 * behaviours.
 */
public interface Goal {
    /**
     * Resets the goal and any resources or state it is holding.
     */
    public void reset();

    /**
     * Updates the goal.
     */
    public void run(GoalSelector selector);

    /**
     * Returns whether the goal is ready to start.
     *
     * @param selector
     *            The selector to use during execution
     * @return Whether the goal can be started.
     */
    public boolean shouldExecute(GoalSelector selector);
}