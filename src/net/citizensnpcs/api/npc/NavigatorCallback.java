package net.citizensnpcs.api.npc;

/**
 * Represents a navigation callback, linked to a {@link Navigator}. Methods
 * should return whether the callback has finished or not.
 * 
 * @author fullwall
 * 
 */
public abstract class NavigatorCallback {
    public boolean onBegin(Navigator navigator) {
        return false;
    }

    public boolean onCancel(Navigator navigator, PathCancelReason reason) {
        return false;
    }

    public boolean onCompletion(Navigator navigator) {
        return false;
    }

    public enum PathCancelReason {
        PLUGIN,
        STUCK,
        REPLACE;
    }
}
