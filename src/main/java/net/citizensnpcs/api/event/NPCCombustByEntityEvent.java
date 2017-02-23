package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

public class NPCCombustByEntityEvent extends NPCCombustEvent {
    private final EntityCombustByEntityEvent event;

    public NPCCombustByEntityEvent(EntityCombustByEntityEvent event, NPC npc) {
        super(event, npc);
        this.event = event;
    }

    /**
     * The combuster can be a WeatherStorm a Blaze, or an Entity holding a FIRE_ASPECT enchanted item.
     * 
     * @return the Entity that set the combustee alight.
     */
    public Entity getCombuster() {
        return event.getCombuster();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
