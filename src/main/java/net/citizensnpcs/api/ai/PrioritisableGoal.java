package net.citizensnpcs.api.ai;

/**
 * A dynamically prioritisable {@link Goal}.
 */
public interface PrioritisableGoal extends Goal {
    int getPriority();
}
