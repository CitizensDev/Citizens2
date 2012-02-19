package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

/**
 * Called when Citizens is reloaded
 */
public class CitizensReloadEvent extends CitizensEvent {
    public CitizensReloadEvent(String name) {
        super();
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