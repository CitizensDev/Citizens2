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

    @Override
    public void reset() {
        super.reset();
        if (executing != null)
            executing.reset();
        executing = null;
        executingIndex = 0;
    }

    @Override
    public BehaviorStatus run() {
        List<Behavior> behaviors = getBehaviors();
        if (executing == null) {
            while ((executing = behaviors.get(executingIndex)) instanceof ParallelBehavior) {
                addParallel(executing);
                executingIndex++;
                if (executingIndex >= behaviors.size())
                    return BehaviorStatus.SUCCESS;
            }
            if (!executing.shouldExecute()) {
                if (retryChildren) {
                    executing = null;
                    return BehaviorStatus.RUNNING;
                } else
                    return BehaviorStatus.FAILURE;
            }
        }
        tickParallel();
        BehaviorStatus status = executing.run();
        switch (status) {
            case RUNNING:
                return BehaviorStatus.RUNNING;
            case FAILURE:
                if (!retryChildren) {
                    return BehaviorStatus.FAILURE;
                } else {
                    executing = null;
                    return BehaviorStatus.RUNNING;
                }
            case RESET_AND_REMOVE:
                behaviors.remove(executingIndex);
                return selectNext(behaviors);
            case SUCCESS:
                executingIndex++;
                return selectNext(behaviors);
            default:
                throw new IllegalStateException();
        }
    }

    private BehaviorStatus selectNext(List<Behavior> behaviors) {
        if (executingIndex >= behaviors.size())
            return BehaviorStatus.SUCCESS;
        while ((executing = behaviors.get(executingIndex)) instanceof ParallelBehavior) {
            addParallel(executing);
            executingIndex++;
            if (executingIndex >= behaviors.size())
                return BehaviorStatus.SUCCESS;
        }
        if (!executing.shouldExecute() && !retryChildren)
            return BehaviorStatus.FAILURE;
        return BehaviorStatus.RUNNING;
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
