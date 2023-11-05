package net.citizensnpcs.api.ai.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.ai.Navigator;

public class NavigationCompleteEvent extends NavigationEvent {
    public NavigationCompleteEvent(Navigator navigator) {
        super(navigator);
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
