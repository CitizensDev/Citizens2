package net.citizensnpcs.api.ai;

import org.junit.Before;
import org.junit.Test;

public class BehaviorTreeTest {
    private GoalController test;

    @Test
    public void selector() {
    }

    @Test
    public void sequence() {
    }

    @Test
    public void decorator() {
    }

    @Before
    public void setUp() {
        test = new SimpleGoalController();
        test.setPaused(false);
    }
}
