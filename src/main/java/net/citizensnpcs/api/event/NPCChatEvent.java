package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Represents an event where an NPC was clicked by a player.
 */
public abstract class NPCChatEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;

    String message;
    
    protected NPCChatEvent(NPC npc, String message) {
        super(npc);
        this.message = message;
    }

    /**
     * Gets the message that the NPC is saying.
     * 
     * @return Message that the NPC is saying.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}