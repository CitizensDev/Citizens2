package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;

import org.junit.Before;
import org.junit.Test;

public class BehaviorTreeTest {
    private GoalController test;

    @Test
    public void selector() {
        test.addGoal(new BehaviorGoalAdapter() {
            @Override
            public void reset() {
            }

            @Override
            public BehaviorStatus run() {
                return null;
            }

            @Override
            public boolean shouldExecute() {
                return false;
            }
        }, 1);
    }

    @Test
    public void sequence() {
    }

    @Before
    public void setUp() {
        test = new SimpleGoalController();
    }
}
