package net.citizensnpcs.api.ai;

import org.junit.Before;
import org.junit.Test;

public class BehaviorTreeTest {
    private GoalController test;

    @Test
    public void decorator() {
    }

    @Test
    public void selector() {
    }

    @Test
    public void sequence() {
    }

    @Before
    public void setUp() {
        test = new SimpleGoalController();
        test.setPaused(false);
    }
}
