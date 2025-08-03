package net.citizensnpcs.api.ai.tree;

import java.util.function.Consumer;

/**
 * Wraps an {@link Behavior} and runs callbacks when the underlying Behavior is finished.
 */
public class Callback extends BehaviorGoalAdapter {
    private final Consumer<BehaviorStatus> cb;
    private final Behavior wrapping;

    private Callback(Behavior wrapping, Consumer<BehaviorStatus> cb) {
        this.wrapping = wrapping;
        this.cb = cb;
    }

    @Override
    public void reset() {
        wrapping.reset();
    }

    @Override
    public BehaviorStatus run() {
        BehaviorStatus status = wrapping.run();
        switch (status) {
            case FAILURE:
            case SUCCESS:
            case RESET_AND_REMOVE:
                cb.accept(status);
            default:
                break;
        }
        return status;
    }

    @Override
    public boolean shouldExecute() {
        return wrapping.shouldExecute();
    }

    public static Callback callback(Behavior wrapping, Consumer<BehaviorStatus> cb) {
        return new Callback(wrapping, cb);
    }
}
