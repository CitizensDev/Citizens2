package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Runs each {@link Behavior} in sequence.
 */
public class Sequence extends Composite {
    private final boolean continueRunning;
    private Behavior executing;
    private int executingIndex = -1;

    private Sequence(boolean retryChildren, Behavior... behaviors) {
        this(retryChildren, Arrays.asList(behaviors));
    }

    private Sequence(boolean retryChildren, Collection<Behavior> behaviors) {
        super(behaviors);
        this.continueRunning = retryChildren;
    }

    private BehaviorStatus getContinuationStatus() {
        resetCurrent();
        if (continueRunning) {
            if (++executingIndex >= getBehaviors().size()) {
                return BehaviorStatus.FAILURE;
            }
            return BehaviorStatus.RUNNING;
        } else {
            return BehaviorStatus.FAILURE;
        }
    }

    @Override
    public void reset() {
        super.reset();
        resetCurrent();
        executingIndex = -1;
    }

    private void resetCurrent() {
        stopExecution(executing);
        executing = null;
    }

    public boolean retryChildren() {
        return continueRunning;
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
                behaviors.remove(executingIndex--);
                return selectNext(behaviors);
            case SUCCESS:
                resetCurrent();
                return selectNext(behaviors);
            default:
                throw new IllegalStateException();
        }
    }

    private BehaviorStatus selectNext(List<Behavior> behaviors) {
        if (++executingIndex >= behaviors.size()) {
            return BehaviorStatus.SUCCESS;
        }
        executing = behaviors.get(executingIndex);
        if (!executing.shouldExecute()) {
            return getContinuationStatus();
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public String toString() {
        return "Sequence [executing=" + executing + ", executingIndex=" + executingIndex + ", retryChildren="
                + continueRunning + ", getBehaviors()=" + getBehaviors() + "]";
    }

    public static Sequence createRetryingSequence(Behavior... behaviors) {
        return createRetryingSequence(Arrays.asList(behaviors));
    }

    /**
     * Creates a <code>retrying</code> sequence that will continue from the current {@link Behavior} if it returns
     * {@link BehaviorStatus#FAILURE} instead of propagating the failure up to its parent.
     */
    public static Sequence createRetryingSequence(Collection<Behavior> behaviors) {
        return new Sequence(true, behaviors);
    }

    public static Sequence createSequence(Behavior... behaviors) {
        return createSequence(Arrays.asList(behaviors));
    }

    /**
     * Creates sequence that will stop executing if the current {@link Behavior} returns {@link BehaviorStatus#FAILURE}.
     */
    public static Sequence createSequence(Collection<Behavior> behaviors) {
        return new Sequence(false, behaviors);
    }
}
