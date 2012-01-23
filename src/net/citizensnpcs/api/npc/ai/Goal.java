package net.citizensnpcs.api.npc.ai;

public interface Goal {
    public void reset();

    public boolean shouldBegin();

    public boolean shouldContinue();

    public void start();

    public void update();
}