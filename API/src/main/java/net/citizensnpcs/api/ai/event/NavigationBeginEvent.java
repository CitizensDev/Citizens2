package net.citizensnpcs.api.ai.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.ai.Navigator;

public class NavigationBeginEvent extends NavigationEvent {
    public NavigationBeginEvent(Navigator navigator) {
        super(navigator);
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
