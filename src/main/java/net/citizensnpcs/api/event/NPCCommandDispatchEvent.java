package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called just before a command list is dispatched.
 */

public class NPCCommandDispatchEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final Player player;

    public NPCCommandDispatchEvent(NPC npc, Player player) {
        super(npc);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return the Player who will be dispatched on
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
