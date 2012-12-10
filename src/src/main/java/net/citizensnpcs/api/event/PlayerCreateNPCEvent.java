package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerCreateNPCEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final Player creator;
    private String reason;

    public PlayerCreateNPCEvent(Player player, NPC npc) {
        super(npc);
        creator = player;
    }

    /**
     * @return The reason for cancelling the event
     * @see #getCancelReason()
     */
    public String getCancelReason() {
        return reason;
    }

    /**
     * @return The {@link Player} creating the NPC.
     */
    public Player getCreator() {
        return creator;
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
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Sets the reason for cancelling the event. This will be sent to the
     * {@link Player} creator to explain why the NPC cannot be created.
     * 
     * @param reason The reason explaining the cancellation
     */
    public void setCancelReason(String reason) {
        this.reason = reason;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
