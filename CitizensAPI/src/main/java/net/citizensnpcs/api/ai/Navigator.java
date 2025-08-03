package net.citizensnpcs.api.ai;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;

/**
 * Represents the pathfinding AI of an {@link NPC}. The navigator can path towards a single target at a time.
 */
public interface Navigator {
    /**
     * Cancels any running navigation towards a target.
     */
    void cancelNavigation();

    /**
     * Cancels any running navigation towards a target with a specific {@link CancelReason}.
     */
    void cancelNavigation(CancelReason reason);

    /**
     * @see #canNavigateTo(Location, NavigatorParameters)
     */
    boolean canNavigateTo(Location dest);

    /**
     * Returns whether the NPC can navigate to the given destination with the navigator parameters.
     */
    boolean canNavigateTo(Location dest, NavigatorParameters params);

    /**
     * Returns the {@link NavigatorParameters} local to this navigator. These parameters are copied to local target
     * parameters when a new target is started.
     *
     * @see #getLocalParameters()
     * @return The default parameters
     */

    NavigatorParameters getDefaultParameters();

    /**
     * Returns the current {@link EntityTarget} of the navigator, if any. May return null.
     *
     * @return The current entity target
     */
    EntityTarget getEntityTarget();

    /**
     * Returns the {@link NavigatorParameters} local to any current target execution. These are updated independently of
     * the default parameters.
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
     *
     * @return The current {@link PathStrategy} or null if the navigator is not pathfinding
     */
    PathStrategy getPathStrategy();

    /**
     * Returns the current {@link Location} being navigated towards - this is not necessarily permanent and may change,
     * for example when pathing towards a moving {@link Entity}. May return null.
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
     * @return Whether the navigator is currently paused
     */
    boolean isPaused();

    /**
     * Sets whether the navigator is paused and shouldn't process the path for now.
     *
     * @param paused
     *            Whether the navigator should be paused or not
     */
    void setPaused(boolean paused);

    /**
     * Sets the current navigation to an entity target. The NPC will walk towards them in a straight line without
     * pathfinding.
     *
     * @param target
     *            The {@link Entity} to walk towards
     * @param aggressive
     *            Whether to attack the target when close enough
     */
    void setStraightLineTarget(Entity target, boolean aggressive);

    /**
     * Sets the current navigation to a {@link Location} destination. The NPC will walk straight towards it without
     * pathfinding.
     *
     * @param target
     *            The destination
     */
    void setStraightLineTarget(Location target);

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
     * Sets the current navigation to the specified strategy.
     *
     * @param strategy
     *            New navigation strategy
     */
    void setTarget(Function<NavigatorParameters, PathStrategy> strategy);

    /**
     * Sets the current navigation using a list of {@link Vector}s which will be moved between sequentially using the
     * Citizens movement logic <em>without</em> pathfinding.
     *
     * @param path
     *            The path
     */
    void setTarget(Iterable<Vector> path);

    /**
     * Sets the current navigation to a {@link Location} destination.
     *
     * @param target
     *            The destination
     */
    void setTarget(Location target);
}
