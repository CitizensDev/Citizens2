package net.citizensnpcs.api.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Represents AI that can be attached to an NPC.
 */
public interface AI {

    /**
     * Registers a {@link Goal} with a given priority.
     * 
     * @param priority
     *            The priority of the goal
     * @param goal
     *            The goal
     */
    public void addGoal(int priority, Goal goal);

    /**
     * Returns whether this AI is currently pathing towards an {@link Entity} or
     * {@link Location}.
     * 
     * @see AI#setDestination(Location)
     * @see AI#setTarget(LivingEntity, boolean)
     * @return Whether the AI has a destination
     */
    public boolean hasDestination();

    /**
     * Registers a callback
     * 
     * @param callback
     *            {@link NavigationCallback} to register
     */
    public void registerNavigationCallback(NavigationCallback callback);

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

    /**
     * Cancels the destination of this AI.
     * 
     * @see AI#setDestination(Location)
     * @see AI#setTarget(LivingEntity, boolean)
     */
    public void cancelDestination();
}