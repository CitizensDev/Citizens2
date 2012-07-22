package net.citizensnpcs.api.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Represents the pathfinding AI of an {@link NPC}. The navigator can path
 * towards a single target at a time.
 */
public interface Navigator {
    /**
     * Cancels any running navigation towards a target.
     */
    void cancelNavigation();

    /**
     * Returns the current {@link EntityTarget} of the navigator, if any. May
     * return null.
     * 
     * @return The current entity target
     */
    EntityTarget getEntityTarget();

    /**
     * Returns the current entity movement speed of the navigator.
     * 
     * @see #getSpeed()
     * @return The entity movement speed
     */
    float getSpeed();

    /**
     * Returns the current {@link Location} being navigated towards - this is
     * not necessarily permanent and may change, for example when pathing
     * towards a moving {@link Entity}. May return null.
     * 
     * @return The target location
     */
    Location getTargetAsLocation();

    /**
     * @return The current {@link TargetType} of the navigator
     */
    TargetType getTargetType();

    /**
     * @see #getTargetType()
     * @return Whether the navigator is currently pathing towards a target.
     */
    boolean isNavigating();

    /**
     * Sets the movement speed of the navigator. The default value is
     * entity-dependent and Minecraft-specific but usually ranges around 0.3.
     * Note that this may not necessarily change the movement speed of the
     * current target.
     * 
     * @param speed
     *            The new movement speed
     */
    void setSpeed(float speed);

    /**
     * Sets the current navigation to an entity target.
     * 
     * @param target
     *            The {@link LivingEntity} to path towards
     * @param aggressive
     *            Whether to attack the target when close enough
     */
    void setTarget(LivingEntity target, boolean aggressive);

    /**
     * Sets the current navigation to a {@link Location} destination.
     * 
     * @param target
     *            The destination
     */
    void setTarget(Location target);
}
