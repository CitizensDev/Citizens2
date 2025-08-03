package net.citizensnpcs.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC spawns.
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;
    private final Location location;
    private final SpawnReason reason;

    public NPCSpawnEvent(NPC npc, Location location, SpawnReason reason) {
        super(npc);
        this.location = location;
        this.reason = reason;
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
        return location.clone();
    }

    /**
     * Gets the reason for spawning the NPC.
     *
     * @return Reason for spawning the NPC
     */
    public SpawnReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}