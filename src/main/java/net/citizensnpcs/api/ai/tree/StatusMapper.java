package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Supplier;

/**
 * Wraps an {@link Behavior} and returns a supplied {@link BehaviorStatus} instead of the underlying status.
 */
public class StatusMapper extends BehaviorGoalAdapter {
    private final Supplier<BehaviorStatus> to;
    private final Behavior wrapping;

    private StatusMapper(Behavior wrapping, Supplier<BehaviorStatus> to) {
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

    public static StatusMapper mapping(Behavior wrapping, Supplier<BehaviorStatus> to) {
        return new StatusMapper(wrapping, to);
    }

    public static Behavior singleUse(Behavior base) {
        return new Behavior() {

            @Override
            public void reset() {
                base.reset();
            }

            @Override
            public BehaviorStatus run() {
                BehaviorStatus status = base.run();
                switch (status) {
                    case FAILURE:
                    case SUCCESS:
                        return BehaviorStatus.RESET_AND_REMOVE;
                    default:
                        return status;
                }
            }

            @Override
            public boolean shouldExecute() {
                return base.shouldExecute();
            }
        };
    }
}
