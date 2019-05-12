package net.citizensnpcs.api.ai.tree;

/**
 * Wraps a {@link Behavior} and enforces a tick limit, after which it will return {@link BehaviorStatus#FAILURE} and
 * reset the child {@link Behavior}.
 */
public class TimerDecorator extends BehaviorGoalAdapter {
    private final int limit;
    private int ticks;
    private final Behavior wrapping;

    private TimerDecorator(Behavior wrapping, int tickLimit) {
        this.limit = tickLimit;
        this.wrapping = wrapping;
    }

    @Override
    public void reset() {
        ticks = 0;
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        if (++ticks >= limit) {
            return BehaviorStatus.FAILURE;
        }
        return wrapping.run();
    }

    @Override
    public boolean shouldExecute() {
        return wrapping.shouldExecute();
    }

    public static TimerDecorator tickLimiter(Behavior wrapping, int tickLimit) {
        return new TimerDecorator(wrapping, tickLimit);
    }
}
