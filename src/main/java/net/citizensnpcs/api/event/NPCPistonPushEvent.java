package net.citizensnpcs.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCPistonPushEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;

    public NPCPistonPushEvent(NPC npc) {
        super(npc);
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
