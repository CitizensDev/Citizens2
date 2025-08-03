package net.citizensnpcs.api.ai;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                if (idx >= input.size()) {
                    idx = 0;
                }
                return b;
            }
        }).retryChildren().build();
        test.addGoal(p, 1);
        test.run();
        assertEquals(1, goal.resetCount, "Reset count first");
        assertEquals(1, goal.runCount, "Run count first");
        assertEquals(1, goal.shouldExecuteCount, "Should execute count first");
        assertEquals(1, goal2.resetCount, "Reset count second");
        assertEquals(1, goal2.runCount, "Run count second");
        assertEquals(1, goal2.shouldExecuteCount, "Should execute count second");
    }

    @Test
    public void failureSequence() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.FAILURE);
        CountedBehavior goal2 = new CountedBehavior(BehaviorStatus.SUCCESS);
        Sequence p = Sequence.createRetryingSequence(goal, goal2);
        test.addGoal(p, 1);
        test.run();
        test.run();
        assertEquals(2, goal.resetCount, "Reset count");
        assertEquals(2, goal.runCount, "Run count");
        assertEquals(2, goal.shouldExecuteCount, "Should execute count");
        assertEquals(0, goal2.resetCount, "Reset count2");
        assertEquals(0, goal2.runCount, "Run count2");
        assertEquals(0, goal2.shouldExecuteCount, "Should execute count2");
    }

    @BeforeEach
    public void setUp() {
        test = new SimpleGoalController();
    }

    @Test
    public void singleSelector() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Selector p = Selector.selecting(goal).build();
        test.addGoal(p, 1);
        test.run();
        assertEquals(1, goal.resetCount, "Reset count");
        assertEquals(1, goal.runCount, "Run count");
        assertEquals(1, goal.shouldExecuteCount, "Should execute count");
    }

    @Test
    public void singleSequence() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Sequence p = Sequence.createSequence(goal);
        test.addGoal(p, 1);
        test.run();
        assertEquals(1, goal.resetCount, "Reset count");
        assertEquals(1, goal.runCount, "Run count");
        assertEquals(1, goal.shouldExecuteCount, "Should execute count");
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
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": reset");
            }
            resetCount++;
        }

        @Override
        public BehaviorStatus run() {
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": run " + ret);
            }
            runCount++;
            return ret;
        }

        @Override
        public boolean shouldExecute() {
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": shouldExecute");
            }
            shouldExecuteCount++;
            return true;
        }
    }
}
