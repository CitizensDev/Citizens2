package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
