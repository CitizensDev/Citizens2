package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A selector of sub-goals, that chooses a single {@link Behavior} to execute
 * from a list. The default selection function is a random selection.
 */
public class Selector extends Composite {
    private Behavior executing;
    private boolean retryChildren = false;
    private final Function<List<Behavior>, Behavior> selectionFunction;

    private int x;

    private Selector(Function<List<Behavior>, Behavior> selectionFunction, boolean retryChildren,
            Collection<Behavior> behaviors) {
        super(behaviors);
        this.selectionFunction = selectionFunction;
        this.retryChildren = retryChildren;
    }

    protected Behavior getNextBehavior() {
        Behavior behavior = null;
        while ((behavior = selectionFunction.apply(getBehaviors())) instanceof ParallelBehavior) {
            addParallel(behavior);
            x++;
        }
        return behavior;
    }

    @Override
    public void reset() {
        super.reset();
        if (executing != null)
            executing.reset();
        executing = null;
    }

    @Override
    public BehaviorStatus run() {
        if (executing == null)
            executing = getNextBehavior();
        tickParallel();
        BehaviorStatus status = executing.run();
        if (status == BehaviorStatus.FAILURE) {
            if (retryChildren) {
                executing.reset();
                executing = getNextBehavior();
                return BehaviorStatus.RUNNING;
            }
        } else if (status == BehaviorStatus.RESET_AND_REMOVE) {
            getBehaviors().remove(executing);
            return BehaviorStatus.SUCCESS;
        }
        return status;
    }

    public static class Builder {
        private final Collection<Behavior> behaviors;
        private boolean retryChildren;
        private Function<List<Behavior>, Behavior> selectionFunction = RANDOM_SELECTION;

        private Builder(Collection<Behavior> behaviors) {
            this.behaviors = behaviors;
        }

        public Selector build() {
            return new Selector(selectionFunction, retryChildren, behaviors);
        }

        /**
         * Sets whether to retry child {@link Behavior}s when they return
         * {@link BehaviorStatus#FAILURE}.
         * 
         * @param retry
         *            Whether to retry children (default: false)
         */
        public Builder retryChildren(boolean retry) {
            retryChildren = retry;
            return this;
        }

        /**
         * Sets the {@link Function} that selects a {@link Behavior} to execute
         * from a list of behaviors, such as a random selection or a priority
         * selection. See {@link Selectors} for some helper methods.
         * 
         * @param function
         *            The selection function
         */
        public Builder selectionFunction(Function<List<Behavior>, Behavior> function) {
            selectionFunction = function;
            return this;
        }
    }

    private static class empty extends BehaviorGoalAdapter {
        @Override
        public void reset() {
        }

        @Override
        public BehaviorStatus run() {
            return null;
        }

        @Override
        public boolean shouldExecute() {
            return false;
        }
    }

    private static class emptyo extends BehaviorGoalAdapter implements ParallelBehavior {
        @Override
        public void reset() {
        }

        @Override
        public BehaviorStatus run() {
            return BehaviorStatus.SUCCESS;
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }
    }

    private static final Random RANDOM = new Random();

    private static final Function<List<Behavior>, Behavior> RANDOM_SELECTION = new Function<List<Behavior>, Behavior>() {
        @Override
        public Behavior apply(@Nullable List<Behavior> behaviors) {
            return behaviors.get(RANDOM.nextInt(behaviors.size()));
        }
    };
    public static void main(String[] args) {
        Collection<Behavior> b = Lists.<Behavior> newArrayList(new empty());
        for (int i = 0; i < 1000; i++) {
            b.add(Math.random() < 0.001 ? new empty() : new emptyo());
        }
        Selector sel = Selector.selecting(b).build();
        for (int i = 0; i < 500; i++)
            sel.run();
        System.err.println(sel.x + " " + sel.getBehaviors().size());
    }

    public static Builder selecting(Behavior... behaviors) {
        return selecting(Arrays.asList(behaviors));
    }

    public static Builder selecting(Collection<Behavior> behaviors) {
        return new Builder(behaviors);
    }
}
