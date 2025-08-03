package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCDamageByEntityEvent extends NPCDamageEvent {
    private final Entity damager;

    public NPCDamageByEntityEvent(NPC npc, EntityDamageByEntityEvent event) {
        super(npc, event);
        damager = event.getDamager();
    }

    public Entity getDamager() {
        return damager;
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
