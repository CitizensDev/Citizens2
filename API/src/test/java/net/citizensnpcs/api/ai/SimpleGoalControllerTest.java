package net.citizensnpcs.api.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleGoalControllerTest {
    GoalController controller;

    @Test
    public void finishAndRemove() {
        controller.addGoal(new FinishAndRemove(), 1);
        controller.run();
        controller.run();
        assertFalse(controller.iterator().hasNext());
    }

    @Test
    public void priority() {
        ImmediateFinish one = new ImmediateFinish();
        ImmediateFinish two = new ImmediateFinish();
        ImmediateFinish three = new ImmediateFinish();
        controller.addGoal(one, 1);
        controller.addGoal(two, 2);
        controller.addGoal(three, 3);

        controller.run();
        assertEquals(0, one.counter);
        assertEquals(0, two.counter);
        assertNotEquals(0, three.counter);
    }

    @Test
    public void random() {
        ImmediateFinish one = new ImmediateFinish();
        ImmediateFinish two = new ImmediateFinish();
        controller.addGoal(new ImmediateFinish(), 2);
        controller.addGoal(new ImmediateFinish(), 1);
        controller.addGoal(one, 2);
        controller.addGoal(two, 2);

        for (int i = 0; i < 100; i++) {
            controller.run();
        }
        assertNotEquals(0, one.counter);
        assertNotEquals(0, two.counter);
    }

    @BeforeEach
    public void setUp() {
        controller = new SimpleGoalController();
    }

    public static class FinishAndRemove implements Goal {
        @Override
        public void reset() {
        }

        @Override
        public void run(GoalSelector selector) {
            selector.finishAndRemove();
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            return true;
        }
    }

    public static class ImmediateFinish implements Goal {
        int counter = 0, maxTimes = 1, times = 0;

        public ImmediateFinish() {
        }

        public ImmediateFinish(int times) {
            maxTimes = times;
        }

        @Override
        public void reset() {
            times = 0;
            counter++;
        }

        @Override
        public void run(GoalSelector selector) {
            if (++times >= maxTimes) {
                selector.finish();
            }
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            return true;
        }
    }
}
