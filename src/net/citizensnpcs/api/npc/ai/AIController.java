package net.citizensnpcs.api.npc.ai;

public interface AIController {
    /**
     * Registers a {@link Goal} with a given priority.
     * 
     * @param priority
     *            The priority of the goal
     * @param goal
     *            The goal
     */
    public void addGoal(int priority, Goal goal);

    /**
     * Sets the AI of this {@link AIController} as a {@link Runnable}.
     * 
     * @param ai
     */
    public void setAI(Runnable ai);
}