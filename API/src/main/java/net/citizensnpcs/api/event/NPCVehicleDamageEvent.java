package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCVehicleDamageEvent extends NPCEvent implements Cancellable {
    private final Entity damager;
    private final VehicleDamageEvent event;

    public NPCVehicleDamageEvent(NPC npc, VehicleDamageEvent event) {
        super(npc);
        this.event = event;
        damager = event.getAttacker();
    }

    public Entity getDamager() {
        return damager;
    }

    public VehicleDamageEvent getEvent() {
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
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
