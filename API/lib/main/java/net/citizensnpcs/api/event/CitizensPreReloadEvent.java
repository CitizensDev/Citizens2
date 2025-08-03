package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

/**
 * Called just before Citizens is reloaded.
 */
public class CitizensPreReloadEvent extends CitizensEvent {
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}