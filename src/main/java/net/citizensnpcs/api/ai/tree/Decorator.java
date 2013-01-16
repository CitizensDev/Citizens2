package net.citizensnpcs.api.ai.tree;

import java.util.Collection;
import java.util.Collections;

import net.citizensnpcs.api.ai.GoalStatus;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Decorator extends BehaviorGoalAdapter {
    private final Collection<Runnable> resetCallbacks;
    private final Collection<Runnable> runCallbacks;
    private final Collection<Function<Boolean, Boolean>> shouldExecuteTransformers;
    private final Collection<Function<GoalStatus, GoalStatus>> statusTransformers;
    private final Behavior wrapping;

    private Decorator(Behavior toWrap, Collection<Runnable> runCallbacks,
            Collection<Function<GoalStatus, GoalStatus>> statusTransformers,
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
    public GoalStatus run() {
        for (Runnable runnable : runCallbacks) {
            runnable.run();
        }
        GoalStatus status = wrapping.run();
        for (Function<GoalStatus, GoalStatus> transformer : statusTransformers) {
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
        private Collection<Function<GoalStatus, GoalStatus>> statusTransformers = Collections.emptyList();
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

        public Builder withStatusTransformer(Function<GoalStatus, GoalStatus> transformer) {
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
