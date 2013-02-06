package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Supplier;

public class StatusCoercer extends BehaviorGoalAdapter {
    private final Supplier<BehaviorStatus> to;
    private final Behavior wrapping;

    private StatusCoercer(Behavior wrapping, Supplier<BehaviorStatus> to) {
        this.wrapping = wrapping;
        this.to = to;
    }

    @Override
    public void reset() {
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        return to.get();
    }

    @Override
    public boolean shouldExecute() {
        return wrapping.shouldExecute();
    }

    public static StatusCoercer coercing(Behavior wrapping, Supplier<BehaviorStatus> to) {
        return new StatusCoercer(wrapping, to);
    }
}
