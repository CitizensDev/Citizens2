package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCCollisionEvent extends NPCEvent {
    private final Entity entity;

    public NPCCollisionEvent(NPC npc, Entity entity) {
        super(npc);
        this.entity = entity;
    }

    /**
     * Returns the {@link Entity} that collided with the {@link NPC}.
     *
     * @return The collided entity
     */
    public Entity getCollidedWith() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
