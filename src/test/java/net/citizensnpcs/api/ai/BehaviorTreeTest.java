package net.citizensnpcs.api.ai;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;

public class BehaviorTreeTest {
    private GoalController test;

    @Test
    public void failureSelector() {
        final CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        CountedBehavior goal2 = new CountedBehavior(BehaviorStatus.FAILURE);
        Selector p = Selector.selecting(goal2, goal).selectionFunction(new Function<List<Behavior>, Behavior>() {
            int idx;

            @Override
            public Behavior apply(List<Behavior> input) {
                Behavior b = input.get(idx++);
                if (idx >= input.size())
                    idx = 0;
                return b;
            }
        }).retryChildren().build();
        test.addGoal(p, 1);
        test.run();
        assertThat("Reset count first", goal.resetCount, is(1));
        assertThat("Run count first", goal.runCount, is(1));
        assertThat("Should execute count first", goal.shouldExecuteCount, is(1));
        assertThat("Reset count second", goal2.resetCount, is(1));
        assertThat("Run count second", goal2.runCount, is(1));
        assertThat("Should execute count second", goal2.shouldExecuteCount, is(1));
    }

    @Test
    public void failureSequence() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.FAILURE);
        CountedBehavior goal2 = new CountedBehavior(BehaviorStatus.SUCCESS);
        Sequence p = Sequence.createRetryingSequence(goal, goal2);
        test.addGoal(p, 1);
        test.run();
        test.run();
        assertThat("Reset count", goal.resetCount, is(1));
        assertThat("Run count", goal.runCount, is(1));
        assertThat("Should execute count", goal.shouldExecuteCount, is(1));
        assertThat("Reset count2", goal2.resetCount, is(1));
        assertThat("Run count2", goal2.runCount, is(1));
        assertThat("Should execute count2", goal2.shouldExecuteCount, is(1));
    }

    @Before
    public void setUp() {
        test = new SimpleGoalController();
    }

    @Test
    public void singleSelector() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Selector p = Selector.selecting(goal).build();
        test.addGoal(p, 1);
        test.run();
        assertThat("Reset count", goal.resetCount, is(1));
        assertThat("Run count", goal.runCount, is(1));
        assertThat("Should execute count", goal.shouldExecuteCount, is(1));
    }

    @Test
    public void singleSequence() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Sequence p = Sequence.createSequence(goal);
        test.addGoal(p, 1);
        test.run();
        assertThat("Reset count", goal.resetCount, is(1));
        assertThat("Run count", goal.runCount, is(1));
        assertThat("Should execute count", goal.shouldExecuteCount, is(1));
    }

    private static class CountedBehavior extends BehaviorGoalAdapter {
        public int loggingTag = 0;
        private int resetCount;
        private final BehaviorStatus ret;
        private int runCount;
        private int shouldExecuteCount;

        private CountedBehavior(BehaviorStatus ret) {
            this.ret = ret;
        }

        @Override
        public void reset() {
            if (loggingTag > 0)
                System.err.println(loggingTag + ": reset");
            resetCount++;
        }

        @Override
        public BehaviorStatus run() {
            if (loggingTag > 0)
                System.err.println(loggingTag + ": run " + ret);
            runCount++;
            return ret;
        }

        @Override
        public boolean shouldExecute() {
            if (loggingTag > 0)
                System.err.println(loggingTag + ": shouldExecute");
            shouldExecuteCount++;
            return true;
        }
    }
}
