package net.citizensnpcs.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC teleports.
 */
public class NPCTeleportEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final Location to;

    public NPCTeleportEvent(NPC npc, Location to) {
        super(npc);
        this.to = to;
    }

    public Location getFrom() {
        return npc.getStoredLocation();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Location getTo() {
        return to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}