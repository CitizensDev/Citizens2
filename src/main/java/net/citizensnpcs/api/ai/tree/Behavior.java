package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.GoalStatus;

public interface Behavior {
    void reset();

    GoalStatus run();

    boolean shouldExecute();
}
