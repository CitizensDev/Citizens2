package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

public class CitizensEnableEvent extends CitizensEvent {
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
