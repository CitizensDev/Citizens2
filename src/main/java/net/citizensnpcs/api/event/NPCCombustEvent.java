package net.citizensnpcs.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCCombustEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final EntityCombustEvent event;

    public NPCCombustEvent(EntityCombustEvent event, NPC npc) {
        super(npc);
        this.event = event;
    }

    public EntityCombustEvent getHandle() {
        return event;
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
