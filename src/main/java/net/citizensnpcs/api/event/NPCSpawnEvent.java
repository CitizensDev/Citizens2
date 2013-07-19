package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when an NPC spawns.
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;

    private final Location location;

    public NPCSpawnEvent(NPC npc, Location location) {
        super(npc);
        this.location = location;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the location where the NPC was spawned.
     * 
     * @return Location where the NPC was spawned
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}