package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.event.HandlerList;

import com.google.common.collect.Lists;

/**
 * The base class for composite {@link Behavior}s, which handle the transition
 * between multiple sub-behaviors.
 */
public abstract class Composite extends BehaviorGoalAdapter {
    private final List<Behavior> behaviors;
    private final Collection<Behavior> parallelExecuting = Lists.newArrayListWithCapacity(0);

    public Composite(Behavior... behaviors) {
        this(Arrays.asList(behaviors));
    }

    public Composite(Collection<Behavior> behaviors) {
        this.behaviors = Lists.newArrayList(behaviors);
        boolean foundNonParallel = false;
        for (Behavior behavior : behaviors) {
            if (!(behavior instanceof ParallelBehavior)) {
                foundNonParallel = true;
                break;
            }
        }
        if (!foundNonParallel)
            throw new IllegalStateException("must have at least one non-parallel node");
    }

    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    protected void addParallel(Behavior behavior) {
        if (behavior.shouldExecute() && !parallelExecuting.contains(behavior)) {
            parallelExecuting.add(behavior);
            prepareForExecution(behavior);
        }
    }

    protected List<Behavior> getBehaviors() {
        return behaviors;
    }

    protected void prepareForExecution(Behavior behavior) {
        if (behavior == null)
            return;
        CitizensAPI.registerEvents(behavior);
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    @Override
    public void reset() {
        if (parallelExecuting.size() > 0) {
            for (Behavior behavior : parallelExecuting)
                behavior.reset();
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
        HandlerList.unregisterAll(behavior);
        behavior.reset();
    }

    protected void tickParallel() {
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
}
