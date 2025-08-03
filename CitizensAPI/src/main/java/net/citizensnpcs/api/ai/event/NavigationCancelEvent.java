package net.citizensnpcs.api.ai.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.ai.Navigator;

public class NavigationCancelEvent extends NavigationCompleteEvent {
    private final CancelReason reason;

    public NavigationCancelEvent(Navigator navigator, CancelReason reason) {
        super(navigator);
        this.reason = reason;
    }

    /**
     * @return The cancellation reason
     */
    public CancelReason getCancelReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
