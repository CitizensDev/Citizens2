package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class NPCVehicleExitEvent extends NPCEvent {
    private final LivingEntity entity;

    public NPCVehicleExitEvent(NPC npc, LivingEntity entity) {
        super(npc);
        this.entity = entity;
    }

    public LivingEntity getExited() {
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
