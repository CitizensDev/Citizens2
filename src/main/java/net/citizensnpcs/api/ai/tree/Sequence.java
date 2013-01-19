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
        if (executing != null)
            executing.reset();
        executing = null;
        executingIndex = 0;
    }

    @Override
    public BehaviorStatus run() {
        List<Behavior> behaviors = getBehaviors();
        if (executing == null) {
            executing = behaviors.get(executingIndex);
            if (!executing.shouldExecute()) {
                if (retryChildren) {
                    executing = null;
                    return BehaviorStatus.RUNNING;
                } else
                    return BehaviorStatus.FAILURE;
            }
        }
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
            case SUCCESS:
                executingIndex++;
                if (executingIndex >= behaviors.size())
                    return BehaviorStatus.SUCCESS;
                executing = behaviors.get(executingIndex);
                if (!executing.shouldExecute() && !retryChildren)
                    return BehaviorStatus.FAILURE;
                return BehaviorStatus.RUNNING;
            default:
                throw new IllegalStateException();
        }
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
