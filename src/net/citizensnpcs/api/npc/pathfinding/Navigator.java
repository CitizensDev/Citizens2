package net.citizensnpcs.api.npc.pathfinding;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Handles pathfinding for an NPC
 */
public interface Navigator {

    /**
     * Creates a path to the given destination
     * 
     * @param destination
     *            Destination of the path
     */
    public void setDestination(Location destination);

    /**
     * Registers a callback for this navigator
     * 
     * @param callback
     *            NavigatorCallback to register
     */
    public void registerCallback(NavigatorCallback callback);

    /**
     * Targets an entity
     * 
     * @param target
     *            Entity to target
     * @param aggressive
     *            Whether the targeting entity should attack
     */
    public void setTarget(Entity target, boolean aggressive);
}