package net.citizensnpcs.api.ai.tree;

/**
 * An empty leaf {@link Behavior}.
 */
public class Empty extends BehaviorGoalAdapter {
    private Empty() {
    }

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

    public static Empty INSTANCE = new Empty();
}