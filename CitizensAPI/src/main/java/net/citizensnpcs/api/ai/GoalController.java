package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.GoalController.GoalEntry;
import net.citizensnpcs.api.ai.tree.Behavior;

/**
 * Represents a collection of goals that are prioritised and executed, allowing behaviour trees via a
 * {@link GoalSelector} or by implementing {@link Behavior}.
 * <p>
 * In general, using {@link Behavior} is preferred due to mapping more closely to traditional behavior trees.
 * <p>
 * The highest priority {@link Goal} that returns true in {@link Goal#shouldExecute(GoalSelector)} is executed. Any
 * existing goals with a lower priority are replaced via {@link Goal#reset()}.
 */
public interface GoalController extends Runnable, Iterable<GoalEntry> {
    /**
     * Registers a {@link Behavior} with a given priority.
     *
     * @see #addGoal(Goal, int)
     * @param behavior
     *            The behavior
     * @param priority
     *            The priority
     */
    void addBehavior(Behavior behavior, int priority);

    /**
     * Registers a {@link Goal} with a given priority. Priority must be greater than 0.
     *
     * @param priority
     *            The goal priority
     * @param goal
     *            The goal
     */
    void addGoal(Goal goal, int priority);

    /**
     * Registers a goal which can re-prioritise itself dynamically every tick.
     * <p>
     * Implementation note: This may slow down individual goal controller ticks,
     * as the list must be sorted every tick.
     *
     * @param goal
     *            A new {@link PrioritisableGoal}
     */
    void addPrioritisableGoal(PrioritisableGoal goal);

    /**
     * Cancels and resets the currently executing goal.
     */
    void cancelCurrentExecution();

    /**
     * Clears the goal controller of all {@link Goal}s. Will stop the execution of any current goal.
     */
    void clear();

    /**
     * @return Whether a goal is currently being executed
     */
    boolean isExecutingGoal();

    /**
     * @see #setPaused(boolean)
     * @return Whether the controller is currently paused
     */
    boolean isPaused();

    /**
     * Removes the given {@link Behavior} from rotation.
     *
     * @param behavior
     *            The behavior to remove
     */
    void removeBehavior(Behavior behavior);

    /**
     * Removes a {@link Goal} from rotation.
     *
     * @param goal
     *            The goal to remove
     */
    void removeGoal(Goal goal);

    /**
     * Sets whether the controller is paused. While paused, no new {@link Goal}s will be selected and any executing
     * goals will be suspended.
     *
     * @param paused
     *            Whether to pause execution
     */
    void setPaused(boolean paused);

    public static interface GoalEntry extends Comparable<GoalEntry> {
        /**
         * @return The {@link Behavior} held by this entry if it holds one, otherwise null
         */
        Behavior getBehavior();

        /**
         * @return The {@link Goal} held by this entry
         */
        Goal getGoal();

        /**
         * @return The goal's priority
         */
        int getPriority();
    }
}
