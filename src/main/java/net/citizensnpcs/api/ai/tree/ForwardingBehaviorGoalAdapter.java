package net.citizensnpcs.api.ai.tree;

/**
 * Forwards all calls to a secondary {@link Behavior}.
 */
public class ForwardingBehaviorGoalAdapter extends BehaviorGoalAdapter {
    private final Behavior behavior;

    public ForwardingBehaviorGoalAdapter(Behavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ForwardingBehaviorGoalAdapter other = (ForwardingBehaviorGoalAdapter) obj;
        if (behavior == null) {
            if (other.behavior != null) {
                return false;
            }
        } else if (!behavior.equals(other.behavior)) {
            return false;
        }
        return true;
    }

    public Behavior getWrapped() {
        return behavior;
    }

    @Override
    public int hashCode() {
        return 31 + ((behavior == null) ? 0 : behavior.hashCode());
    }

    @Override
    public void reset() {
        behavior.reset();
    }

    @Override
    public BehaviorStatus run() {
        return behavior.run();
    }

    @Override
    public boolean shouldExecute() {
        return behavior.shouldExecute();
    }

    @Override
    public String toString() {
        return "ForwardingBehaviorGoalAdapter [behavior=" + behavior + "]";
    }
}