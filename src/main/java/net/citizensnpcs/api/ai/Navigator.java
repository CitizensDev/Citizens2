package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
     * Returns the {@link NavigatorParameters} local to this navigator. These
     * parameters are copied to local target parameters when a new target is
     * started.
     * 
     * @see #getLocalParameters()
     * @return The default parameters
     */

    NavigatorParameters getDefaultParameters();

    /**
     * Returns the current {@link EntityTarget} of the navigator, if any. May
     * return null.
     * 
     * @return The current entity target
     */
    EntityTarget getEntityTarget();

    /**
     * Returns the {@link NavigatorParameters} local to any current target
     * execution. These are updated independently of the default parameters.
     * 
     * @see #getDefaultParameters()
     * @return The local parameters
     */
    NavigatorParameters getLocalParameters();

    /**
     * @return The {@link NPC} attached to this navigator
     */
    NPC getNPC();

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
     * Sets the current navigation to an entity target.
     * 
     * @param target
     *            The {@link Entity} to path towards
     * @param aggressive
     *            Whether to attack the target when close enough
     */
    void setTarget(Entity target, boolean aggressive);

    /**
     * Sets the current navigation to an entity target.
     * 
     * @param target
     *            The {@link LivingEntity} to path towards
     * @param aggressive
     *            Whether to attack the target when close enough
     * @deprecated See {@link #setTarget(Entity, boolean)}
     */
    @Deprecated
    void setTarget(LivingEntity target, boolean aggressive);

    /**
     * Sets the current navigation to a {@link Location} destination.
     * 
     * @param target
     *            The destination
     */
    void setTarget(Location target);
}
