package net.citizensnpcs.api.npc.ai;

public interface AIController {
    public void setAI(Runnable ai);

    public void addGoal(int priority, Goal goal);
}
