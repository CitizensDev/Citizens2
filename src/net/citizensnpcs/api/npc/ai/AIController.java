package net.citizensnpcs.api.npc.ai;

public interface AIController {
    /**
     * Sets the AI of this {@link AIController} as a {@link Runnable}.
     * 
     * @param ai
     */
    public void setAI(Runnable ai);

    public void addGoal(int priority, Goal goal);
}