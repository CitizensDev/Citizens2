package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The base class for composite {@link Behavior}s, which handle the transition between multiple sub-behaviors.
 */
public abstract class Composite extends BehaviorGoalAdapter {
    private final List<Behavior> behaviors;
    private final List<Behavior> parallel = Lists.newArrayListWithCapacity(0);
    private final Set<Behavior> parallelExecuting = Sets.newHashSetWithExpectedSize(0);

    public Composite(Behavior... behaviors) {
        this(Arrays.asList(behaviors));
    }

    public Composite(Collection<Behavior> behaviors) {
        this.behaviors = Lists.newArrayList(behaviors);
        Iterator<Behavior> itr = this.behaviors.iterator();
        while (itr.hasNext()) {
            Behavior b = itr.next();
            if (b instanceof ParallelBehavior) {
                parallel.add(b);
                itr.remove();
            }
        }
    }

    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    public List<Behavior> getBehaviors() {
        return behaviors;
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    @Override
    public void reset() {
        if (parallelExecuting.size() > 0) {
            for (Behavior behavior : parallelExecuting) {
                stopExecution(behavior);
            }
            parallelExecuting.clear();
        }
    }

    @Override
    public boolean shouldExecute() {
        return behaviors.size() > 0;
    }

    protected void stopExecution(Behavior behavior) {
        if (behavior == null)
            return;
        behavior.reset();
    }

    protected void tickParallel() {
        for (Behavior b : parallel) {
            tryAddParallel(b);
        }
        Iterator<Behavior> itr = parallelExecuting.iterator();
        while (itr.hasNext()) {
            Behavior behavior = itr.next();
            BehaviorStatus status = behavior.run();
            switch (status) {
                case RESET_AND_REMOVE:
                    behaviors.remove(behavior);
                case FAILURE:
                case SUCCESS:
                    itr.remove();
                    stopExecution(behavior);
                    break;
                default:
                    break;
            }
        }
    }

    private void tryAddParallel(Behavior behavior) {
        if (behavior.shouldExecute() && !parallelExecuting.contains(behavior)) {
            parallelExecuting.add(behavior);
        }
    }
}
