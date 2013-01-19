package net.citizensnpcs.api.ai.tree;

public enum BehaviorStatus {
    /**
     * Indicates that the {@link Behavior} has failed unrecoverably.
     */
    FAILURE,
    /**
     * Indicates that the {@link Behavior} is still running and should be
     * continued next tick.
     */
    RUNNING,
    /**
     * Indicates that the {@link Behavior} has succeeded and can be terminated.
     */
    SUCCESS;
}
