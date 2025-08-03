package net.citizensnpcs.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}