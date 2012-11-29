package net.citizensnpcs.api.ai;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SimpleGoalControllerTest {
    GoalController controller;

    @Test
    public void finishAndRemove() {
        controller.addGoal(new FinishAndRemove(), 1);
        controller.run();
        controller.run();
        assertThat(controller.iterator().hasNext(), is(false));
    }

    @Test
    public void random() {
        ImmediateFinish one = new ImmediateFinish();
        ImmediateFinish two = new ImmediateFinish();
        controller.addGoal(one, 1);
        controller.addGoal(two, 1);

        for (int i = 0; i < 100; i++) {
            controller.run();
        }
        assertThat(one.counter, not(0));
        assertThat(two.counter, not(0));
    }

    @Before
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
            if (++times >= maxTimes)
                selector.finish();
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            return true;
        }
    }
}
