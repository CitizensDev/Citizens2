package net.citizensnpcs;

import net.citizensnpcs.api.event.CitizensEvent;

import org.bukkit.event.HandlerList;

public class CitizensDisableEvent extends CitizensEvent {
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
