package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

/**
 * Called when Citizens is reloaded
 */
public class CitizensReloadEvent extends CitizensEvent {
    private static final long serialVersionUID = -3880546787412641097L;
    private static final HandlerList handlers = new HandlerList();

    public CitizensReloadEvent(String name) {
        super("CitizensReloadEvent");
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}