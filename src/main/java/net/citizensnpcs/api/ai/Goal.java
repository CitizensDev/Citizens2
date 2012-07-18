package net.citizensnpcs.api.ai;

import org.bukkit.event.Listener;

/**
 * Represents a Goal that can be added to a {@link GoalController}.
 */
public interface Goal extends Listener {
    /**
     * Resets the goal and any resources or state it is holding.
     */
    public void reset();

    /**
     * Updates the goal.
     */
    public void run();

    /**
     * Returns whether the goal is ready to start.
     * 
     * @param selector
     *            The selector to use during execution
     * @return Whether the goal can be started.
     */
    public boolean shouldExecute(GoalSelector selector);
}