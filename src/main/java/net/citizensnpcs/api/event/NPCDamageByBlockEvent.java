package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

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

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
