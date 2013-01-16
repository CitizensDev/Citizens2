package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.citizensnpcs.api.ai.GoalStatus;

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
    }

    @Override
    public GoalStatus run() {
        List<Behavior> behaviors = getBehaviors();
        if (executing == null) {
            executingIndex = 0;
            executing = behaviors.get(executingIndex);
            if (!executing.shouldExecute())
                return GoalStatus.FAILURE;
        }
        GoalStatus status = executing.run();
        switch (status) {
            case RUNNING:
                return GoalStatus.RUNNING;
            case FAILURE:
                if (!retryChildren)
                    return GoalStatus.FAILURE;
            case SUCCESS:
                executingIndex++;
                if (executingIndex >= behaviors.size())
                    return GoalStatus.SUCCESS;
                executing = behaviors.get(executingIndex);
                if (!executing.shouldExecute())
                    return GoalStatus.FAILURE;
            default:
                throw new IllegalStateException();
        }
    }

    public static Sequence createRetryingSequence(Behavior... behaviors) {
        return new Sequence(true, behaviors);
    }

    public static Sequence createSequence(Behavior... behaviors) {
        return new Sequence(false, behaviors);
    }
}
