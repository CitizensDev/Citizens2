package net.citizensnpcs.api.npc.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Handles pathfinding for an NPC
 */
public interface Navigator {

    /**
     * Registers a callback for this navigator
     * 
     * @param callback
     *            NavigatorCallback to register
     */
    public void registerCallback(NavigatorCallback callback);

    /**
     * Creates a path to the given destination
     * 
     * @param destination
     *            Destination of the path
     */
    public void setDestination(Location destination);

    /**
     * Targets a {@link LivingEntity}
     * 
     * @param target
     *            Entity to target
     * @param aggressive
     *            Whether the targeting entity should attack
     */
    public void setTarget(LivingEntity target, boolean aggressive);
}