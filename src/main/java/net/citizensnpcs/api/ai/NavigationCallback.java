package net.citizensnpcs.api.ai;

/**
 * Represents a navigation callback, linked to a {@link AI}. Methods should
 * return whether the callback has finished or not.
 */
public abstract class NavigationCallback {

    public boolean onBegin(AI ai) {
        return false;
    }

    public boolean onCancel(AI ai, PathCancelReason reason) {
        return false;
    }

    public boolean onCompletion(AI ai) {
        return false;
    }

    public enum PathCancelReason {
        PLUGIN,
        STUCK,
        REPLACE;
    }
}