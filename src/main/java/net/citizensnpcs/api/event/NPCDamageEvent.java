package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
    private final Entity damager;
    private boolean cancelled = true;

    public NPCDamageEvent(NPC npc, Entity damager) {
        super(npc);
        this.damager = damager;
    }

    public Entity getDamager() {
        return this.damager;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
