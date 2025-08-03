package net.citizensnpcs.api.ai.tree;

/**
 * A decorator {@link Behavior} that continues to execute its child behavior as long as a {@link Condition} returns
 * <code>true</code> and the behavior returns {@link BehaviorStatus#SUCCESS}.
 */
public class Loop extends BehaviorGoalAdapter {
    private final Condition condition;
    private final Behavior wrapping;

    public Loop(Behavior wrapping, Condition condition) {
        this.wrapping = wrapping;
        this.condition = condition;
    }

    @Override
    public void reset() {
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        BehaviorStatus status = wrapping.run();
        if (status == BehaviorStatus.SUCCESS) {
            wrapping.reset();
            if (condition.get() && wrapping.shouldExecute())
                return BehaviorStatus.RUNNING;
        }
        return status;
    }

    @Override
    public boolean shouldExecute() {
        return wrapping.shouldExecute();
    }

    public static Loop createWithCondition(Behavior wrapping, Condition condition) {
        return new Loop(wrapping, condition);
    }
}
