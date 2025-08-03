package net.citizensnpcs.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.citizensnpcs.api.npc.NPC;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
    private final EntityDamageEvent event;

    public NPCDamageEvent(NPC npc, EntityDamageEvent event) {
        super(npc);
        this.event = event;
    }

    public DamageCause getCause() {
        return event.getCause();
    }

    public double getDamage() {
        return event.getDamage();
    }

    public EntityDamageEvent getEvent() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public void setDamage(double damage) {
        event.setDamage(damage);
    }

    public void setDamage(int damage) {
        event.setDamage(damage);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
