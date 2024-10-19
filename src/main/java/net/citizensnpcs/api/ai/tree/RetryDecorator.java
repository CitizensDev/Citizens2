package net.citizensnpcs.api.ai.tree;

/**
 * Decorates a {@link Behavior} and retries failures a certain number of times.
 */
public class RetryDecorator extends BehaviorGoalAdapter {
    private final int limit;
    private int retries;
    private final Behavior wrapping;

    private RetryDecorator(Behavior wrapping, int limit) {
        this.limit = limit;
        this.wrapping = wrapping;
    }

    @Override
    public void reset() {
        retries = 0;
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        BehaviorStatus status = wrapping.run();
        if (status == BehaviorStatus.FAILURE) {
            if (limit < 0 || ++retries < limit) {
                reset();
                return BehaviorStatus.RUNNING;
            }
        }
        return status;
    }

    @Override
    public boolean shouldExecute() {
        return wrapping.shouldExecute();
    }

    public static RetryDecorator retry(Behavior wrapping, int n) {
        return new RetryDecorator(wrapping, n);
    }

    public static RetryDecorator unlimited(Behavior wrapping) {
        return retry(wrapping, -1);
    }
}
