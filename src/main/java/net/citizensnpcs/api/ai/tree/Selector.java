package net.citizensnpcs.api.ai.tree;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.citizensnpcs.api.ai.GoalStatus;

import com.google.common.base.Function;

public class Selector extends Composite {
    private Behavior executing;
    private boolean retryChildren = false;
    private final Function<List<Behavior>, Behavior> selectionFunction;

    private Selector(Function<List<Behavior>, Behavior> selectionFunction, boolean retryChildren,
            Collection<Behavior> behaviors) {
        super(behaviors);
        this.selectionFunction = selectionFunction;
        this.retryChildren = retryChildren;
    }

    protected Behavior getNextBehavior() {
        return selectionFunction.apply(getBehaviors());
    }

    @Override
    public void reset() {
        if (executing != null)
            executing.reset();
        executing = null;
    }

    @Override
    public GoalStatus run() {
        if (executing == null) {
            executing = getNextBehavior();
        }
        GoalStatus status = executing.run();
        if (status == GoalStatus.FAILURE) {
            if (retryChildren) {
                executing.reset();
                executing = getNextBehavior();
                return GoalStatus.RUNNING;
            }
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

        public Builder retryChildren(boolean retry) {
            retryChildren = retry;
            return this;
        }

        public Builder selectionFunction(Function<List<Behavior>, Behavior> function) {
            selectionFunction = function;
            return this;
        }
    }

    private static final Random RANDOM = new Random();
    private static final Function<List<Behavior>, Behavior> RANDOM_SELECTION = new Function<List<Behavior>, Behavior>() {
        @Override
        public Behavior apply(@Nullable List<Behavior> behaviors) {
            return behaviors.get(RANDOM.nextInt(behaviors.size()));
        }
    };
}
