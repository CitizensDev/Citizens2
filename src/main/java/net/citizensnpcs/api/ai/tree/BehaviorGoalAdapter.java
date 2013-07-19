package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

/**
 * An adapter between {@link Goal} and {@link Behavior}, forwarding the calls
 * correctly.
 */
public abstract class BehaviorGoalAdapter implements Goal, Behavior {
    @Override
    public void run(GoalSelector selector) {
        BehaviorStatus status = run();
        if (status == BehaviorStatus.RESET_AND_REMOVE)
            selector.finishAndRemove();
        if (status == BehaviorStatus.FAILURE || status == BehaviorStatus.SUCCESS) {
            selector.finish();
        }
    }

    @Override
    public boolean shouldExecute(GoalSelector selector) {
        return shouldExecute();
    }
}
