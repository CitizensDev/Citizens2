package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityTargetNPCEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;

    private final EntityTargetEvent event;

    public EntityTargetNPCEvent(EntityTargetEvent event, NPC npc) {
        super(npc);
        this.event = event;
    }

    /**
     * Returns the Entity involved in this event
     * 
     * @return Entity who is involved in this event
     */
    public Entity getEntity() {
        return event.getEntity();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the reason for the targeting
     * 
     * @return The reason
     */
    public TargetReason getReason() {
        return event.getReason();
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
