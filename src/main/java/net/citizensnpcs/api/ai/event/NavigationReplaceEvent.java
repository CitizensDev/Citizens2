package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;

import org.bukkit.event.HandlerList;

public class NavigationReplaceEvent extends NavigationCancelEvent {
    public NavigationReplaceEvent(Navigator navigator) {
        super(navigator, CancelReason.REPLACE);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
