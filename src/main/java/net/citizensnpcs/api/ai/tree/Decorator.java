package net.citizensnpcs.api.ai.tree;

import java.util.Collection;
import java.util.Collections;


import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A decorator is a wrapper over a {@link Behavior}, which can add functionality
 * such as filtering {@link BehaviorStatus}es, conditions, timer loops and more
 * without knowing the internals of the behavior it wraps.
 */
public class Decorator extends BehaviorGoalAdapter {
    private final Collection<Runnable> resetCallbacks;
    private final Collection<Runnable> runCallbacks;
    private final Collection<Function<Boolean, Boolean>> shouldExecuteTransformers;
    private final Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers;
    private final Behavior wrapping;

    private Decorator(Behavior toWrap, Collection<Runnable> runCallbacks,
            Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers,
            Collection<Function<Boolean, Boolean>> shouldExecuteTransformers, Collection<Runnable> resetCallbacks) {
        this.wrapping = toWrap;
        this.runCallbacks = runCallbacks;
        this.statusTransformers = statusTransformers;
        this.shouldExecuteTransformers = shouldExecuteTransformers;
        this.resetCallbacks = resetCallbacks;
    }

    @Override
    public void reset() {
        for (Runnable runnable : resetCallbacks) {
            runnable.run();
        }
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        for (Runnable runnable : runCallbacks) {
            runnable.run();
        }
        BehaviorStatus status = wrapping.run();
        for (Function<BehaviorStatus, BehaviorStatus> transformer : statusTransformers) {
            status = transformer.apply(status);
        }
        return status;
    }

    @Override
    public boolean shouldExecute() {
        boolean shouldExecute = wrapping.shouldExecute();
        for (Function<Boolean, Boolean> transformer : shouldExecuteTransformers) {
            shouldExecute = transformer.apply(shouldExecute);
        }
        return shouldExecute;
    }

    public static class Builder {
        private Collection<Runnable> resetCallbacks = Collections.emptyList();
        private Collection<Runnable> runCallbacks = Collections.emptyList();
        private Collection<Function<Boolean, Boolean>> shouldExecuteTransformers = Collections.emptyList();
        private Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers = Collections.emptyList();
        private final Behavior toWrap;

        private Builder(Behavior toWrap) {
            this.toWrap = toWrap;
        }

        public Decorator build() {
            return new Decorator(toWrap, runCallbacks, statusTransformers, shouldExecuteTransformers, resetCallbacks);
        }

        public Builder withPreRunCallback(Runnable callback) {
            if (runCallbacks == Collections.EMPTY_LIST)
                runCallbacks = Lists.newArrayList();
            runCallbacks.add(callback);
            return this;
        }

        public Builder withResetCallback(Runnable callback) {
            if (resetCallbacks == Collections.EMPTY_LIST)
                resetCallbacks = Lists.newArrayList();
            resetCallbacks.add(callback);
            return this;
        }

        public Builder withShouldExecuteTransformer(Function<Boolean, Boolean> transformer) {
            if (shouldExecuteTransformers == Collections.EMPTY_LIST)
                shouldExecuteTransformers = Lists.newArrayList();
            shouldExecuteTransformers.add(transformer);
            return this;
        }

        public Builder withStatusTransformer(Function<BehaviorStatus, BehaviorStatus> transformer) {
            if (statusTransformers == Collections.EMPTY_LIST)
                statusTransformers = Lists.newArrayList();
            statusTransformers.add(transformer);
            return this;
        }
    }

    public static Decorator.Builder wrapping(Behavior toWrap) {
        return new Builder(toWrap);
    }
}
