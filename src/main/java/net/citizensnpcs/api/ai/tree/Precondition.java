package net.citizensnpcs.api.ai.tree;

public abstract class Precondition extends BehaviorGoalAdapter {
    protected final Condition condition;

    protected Precondition(Condition condition) {
        this.condition = condition;
    }

    private static class RunPrecondition extends Precondition {
        public RunPrecondition(Condition condition) {
            super(condition);
        }

        @Override
        public void reset() {
        }

        @Override
        public BehaviorStatus run() {
            return condition.get() ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }
    }

    private static class WrappingPrecondition extends Precondition {
        private final Behavior wrapping;

        public WrappingPrecondition(Behavior wrapping, Condition condition) {
            super(condition);
            this.wrapping = wrapping;
        }

        @Override
        public void reset() {
            wrapping.reset();
        }

        @Override
        public BehaviorStatus run() {
            return wrapping.run();
        }

        @Override
        public boolean shouldExecute() {
            return condition.get() ? wrapping.shouldExecute() : false;
        }
    }

    /**
     * Creates a {@link Precondition} that returns either {@link BehaviorStatus#SUCCESS} or
     * {@link BehaviorStatus#FAILURE} depending on the underlying {@link Condition}'s return status.
     * 
     * @param condition
     *            The condition to check while executing
     * @return The precondition behavior
     */
    public static Precondition runPrecondition(Condition condition) {
        return new RunPrecondition(condition);
    }

    /**
     * Creates a {@link Precondition} that wraps the <code>shouldExecute</code> method in {@link Behavior}. When
     * <code>shouldExecute</code> is called, the given {@link Condition} will be checked before the wrapped behavior's
     * method is called.
     * 
     * @param wrapping
     *            The behavior to wrap calls to
     * @param condition
     *            The execution condition
     * @return The precondition behavior
     */
    public static Precondition wrappingPrecondition(Behavior wrapping, Condition condition) {
        return new WrappingPrecondition(wrapping, condition);
    }
}
