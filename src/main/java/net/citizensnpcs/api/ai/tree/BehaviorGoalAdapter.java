package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

public abstract class BehaviorGoalAdapter implements Goal, Behavior {
    @Override
    public void run(GoalSelector selector) {
        run();
    }

    @Override
    public boolean shouldExecute(GoalSelector selector) {
        return shouldExecute();
    }
}
