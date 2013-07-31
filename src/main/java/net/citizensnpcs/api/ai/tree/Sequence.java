package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Sequence extends Composite {
    private Behavior executing;
    private int executingIndex;
    private final boolean retryChildren;

    private Sequence(boolean retryChildren, Behavior... behaviors) {
        this(retryChildren, Arrays.asList(behaviors));
    }

    private Sequence(boolean retryChildren, Collection<Behavior> behaviors) {
        super(behaviors);
        this.retryChildren = retryChildren;
    }

    private BehaviorStatus getContinuationStatus() {
        resetCurrent();
        if (retryChildren) {
            if (++executingIndex >= getBehaviors().size())
                return BehaviorStatus.FAILURE;
            return BehaviorStatus.RUNNING;
        } else {
            return BehaviorStatus.FAILURE;
        }
    }

    @Override
    public void reset() {
        super.reset();
        resetCurrent();
        executingIndex = 0;
    }

    private void resetCurrent() {
        stopExecution(executing);
        executing = null;
    }

    @Override
    public BehaviorStatus run() {
        tickParallel();
        List<Behavior> behaviors = getBehaviors();
        if (executing == null) {
            BehaviorStatus next = selectNext(behaviors);
            if (next != BehaviorStatus.RUNNING) {
                resetCurrent();
                return next;
            }
        }
        BehaviorStatus status = executing.run();
        switch (status) {
            case RUNNING:
                return BehaviorStatus.RUNNING;
            case FAILURE:
                return getContinuationStatus();
            case RESET_AND_REMOVE:
                behaviors.remove(executingIndex);
                return selectNext(behaviors);
            case SUCCESS:
                resetCurrent();
                executingIndex++;
                return selectNext(behaviors);
            default:
                throw new IllegalStateException();
        }
    }

    private BehaviorStatus selectNext(List<Behavior> behaviors) {
        if (executingIndex >= behaviors.size()) {
            return BehaviorStatus.SUCCESS;
        }
        while ((executing = behaviors.get(executingIndex)) instanceof ParallelBehavior) {
            addParallel(executing);
            if (++executingIndex >= behaviors.size()) {
                return BehaviorStatus.SUCCESS;
            }
        }
        if (!executing.shouldExecute()) {
            return getContinuationStatus();
        }
        prepareForExecution(executing);
        return BehaviorStatus.RUNNING;
    }

    @Override
    public String toString() {
        return "Sequence [executing=" + executing + ", executingIndex=" + executingIndex + ", retryChildren="
                + retryChildren + ", getBehaviors()=" + getBehaviors() + "]";
    }

    public static Sequence createRetryingSequence(Behavior... behaviors) {
        return createRetryingSequence(Arrays.asList(behaviors));
    }

    public static Sequence createRetryingSequence(Collection<Behavior> behaviors) {
        return new Sequence(true, behaviors);
    }

    public static Sequence createSequence(Behavior... behaviors) {
        return createSequence(Arrays.asList(behaviors));
    }

    public static Sequence createSequence(Collection<Behavior> behaviors) {
        return new Sequence(false, behaviors);
    }
}
