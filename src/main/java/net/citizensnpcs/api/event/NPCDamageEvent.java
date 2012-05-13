package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
    private final EntityDamageEvent event;

    public NPCDamageEvent(NPC npc, EntityDamageEvent event) {
        super(npc);
        this.event = event;
        event.setCancelled(true);
    }

    public int getDamage() {
        return event.getDamage();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public void setDamage(int damage) {
        event.setDamage(damage);
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
