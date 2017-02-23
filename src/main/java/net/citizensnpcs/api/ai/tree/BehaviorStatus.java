package net.citizensnpcs.api.ai.tree;

public enum BehaviorStatus {
    /**
     * Indicates that the {@link Behavior} has failed unrecoverably.
     */
    FAILURE,
    /**
     * Indicates that the {@link Behavior} should be reset and removed by any parent {@link Composite} behavior nodes.
     */
    RESET_AND_REMOVE,
    /**
     * Indicates that the {@link Behavior} is still running and should be continued next tick.
     */
    RUNNING,
    /**
     * Indicates that the {@link Behavior} has succeeded and can be terminated.
     */
    SUCCESS;
}
