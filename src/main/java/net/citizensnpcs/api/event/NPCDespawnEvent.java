package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when an NPC despawns.
 */
public class NPCDespawnEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final DespawnReason reason;

    public NPCDespawnEvent(NPC npc, DespawnReason reason) {
        super(npc);
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public DespawnReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}