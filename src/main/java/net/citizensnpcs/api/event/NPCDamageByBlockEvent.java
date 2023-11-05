package net.citizensnpcs.api.event;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCDamageByBlockEvent extends NPCDamageEvent {
    private final Block damager;

    public NPCDamageByBlockEvent(NPC npc, EntityDamageByBlockEvent event) {
        super(npc, event);
        damager = event.getDamager();
    }

    public Block getDamager() {
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
