package net.citizensnpcs.api.ai.tree;

/**
 * A condition interface suitable for use in {@link Behavior}s such as {@link IfElse} or {@link Loop}.
 */
public interface Condition {
    boolean get();
}