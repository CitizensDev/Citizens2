package net.citizensnpcs.api.ai.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

/**
 * A decorator is a wrapper over a {@link Behavior}, which can add functionality such as filtering
 * {@link BehaviorStatus}es, conditions, timer loops and more without knowing the internals of the behavior it wraps.
 * <p>
 * Note that there are often simpler alternatives to a full-blown decorator, which has to be generic for many different
 * scenarios.
 */
public class Decorator extends BehaviorGoalAdapter {
    private final Collection<Runnable> resetCallbacks;
    private final Collection<Runnable> runCallbacks;
    private final Collection<Predicate<Boolean>> shouldExecutePredicates;
    private final Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers;
    private final Behavior wrapping;

    private Decorator(Behavior toWrap, Collection<Runnable> runCallbacks,
            Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers,
            Collection<Predicate<Boolean>> shouldExecutePredicates, Collection<Runnable> resetCallbacks) {
        this.wrapping = toWrap;
        this.runCallbacks = runCallbacks;
        this.statusTransformers = statusTransformers;
        this.shouldExecutePredicates = shouldExecutePredicates;
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
        for (Predicate<Boolean> transformer : shouldExecutePredicates) {
            shouldExecute = transformer.test(shouldExecute);
        }
        return shouldExecute;
    }

    public static class Builder {
        private Collection<Runnable> resetCallbacks = Collections.emptyList();
        private Collection<Runnable> runCallbacks = Collections.emptyList();
        private Collection<Predicate<Boolean>> shouldExecutePredicates = Collections.emptyList();
        private Collection<Function<BehaviorStatus, BehaviorStatus>> statusTransformers = Collections.emptyList();
        private final Behavior toWrap;

        private Builder(Behavior toWrap) {
            this.toWrap = toWrap;
        }

        public Decorator build() {
            return new Decorator(toWrap, runCallbacks, statusTransformers, shouldExecutePredicates, resetCallbacks);
        }

        public Builder withPreRunCallback(Runnable callback) {
            if (runCallbacks == Collections.EMPTY_LIST) {
                runCallbacks = Lists.newArrayList();
            }
            runCallbacks.add(callback);
            return this;
        }

        public Builder withResetCallback(Runnable callback) {
            if (resetCallbacks == Collections.EMPTY_LIST) {
                resetCallbacks = Lists.newArrayList();
            }
            resetCallbacks.add(callback);
            return this;
        }

        public Builder withShouldExecutePredicate(Predicate<Boolean> predicate) {
            if (shouldExecutePredicates == Collections.EMPTY_LIST) {
                shouldExecutePredicates = Lists.newArrayList();
            }
            shouldExecutePredicates.add(predicate);
            return this;
        }

        public Builder withStatusTransformer(Function<BehaviorStatus, BehaviorStatus> transformer) {
            if (statusTransformers == Collections.EMPTY_LIST) {
                statusTransformers = Lists.newArrayList();
            }
            statusTransformers.add(transformer);
            return this;
        }
    }

    /**
     * @return Returns a decorator that inverts the status i.e. SUCCESS becomes FAILURE, FAILURE becomes SUCCESS and
     *         others are untouched.
     */
    public static Decorator invert(Behavior toWrap) {
        return new Builder(toWrap).withStatusTransformer(s -> s == BehaviorStatus.FAILURE ? BehaviorStatus.SUCCESS
                : s == BehaviorStatus.SUCCESS ? BehaviorStatus.FAILURE : s).build();
    }

    public static Decorator.Builder wrapping(Behavior toWrap) {
        return new Builder(toWrap);
    }
}
