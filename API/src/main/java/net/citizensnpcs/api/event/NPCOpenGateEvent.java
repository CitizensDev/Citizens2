package net.citizensnpcs.api.event;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCOpenGateEvent extends NPCEvent implements Cancellable {
    private final Block block;
    private boolean cancelled;

    public NPCOpenGateEvent(NPC npc, Block block) {
        super(npc);
        this.block = block;
    }

    /**
     * Returns the {@link Block} that was opened.
     *
     * @return The block
     */
    public Block getGate() {
        return block;
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
    public void setCancelled(boolean arg0) {
        cancelled = arg0;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
