package net.citizensnpcs.api.ai;

/**
 * Represents an AI Goal that can be added to a queue of an NPC's goals.
 */
public interface Goal {

    /**
     * Called just before updating the goal. Returns whether the goal should
     * continue to be updated or be cancelled.
     * 
     * @return Whether the goal should continue
     */
    public boolean continueExecuting();

    /**
     * Returns whether this and the other {@link Goal} can be run at the same
     * time.
     * 
     * @param other
     *            The goal to check
     * @return Whether this goal is compatible
     */
    public boolean isCompatibleWith(Goal other);

    /**
     * Resets the goal and any resources or state it is holding.
     */
    public void reset();

    /**
     * Returns whether the goal is ready to start.
     * 
     * @return Whether the goal can be started.
     */
    public boolean shouldExecute();

    /**
     * Sets up the execution of this goal so that it can be updated later.
     * Called initially instead of {@link Goal#update()};
     */
    public void start();

    /**
     * Updates the goal.
     */
    public void update();
}