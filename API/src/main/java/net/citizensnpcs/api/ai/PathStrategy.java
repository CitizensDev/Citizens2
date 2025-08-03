package net.citizensnpcs.api.ai;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.ai.event.CancelReason;

/**
 * A pathfinding strategy directed at a target. Has two states: pathfinding -> cancelled represented by
 * {@link #getCancelReason()}.
 */
public interface PathStrategy {
    /**
     * Clears the CancelReason returned by {@link #getCancelReason()} and attempts to resume pathfinding.
     */
    void clearCancelReason();

    /**
     * @return The reason for the pathfinding to stop, or null if it is still continuing.
     */
    CancelReason getCancelReason();

    /**
     *
     * @return Gets the current movement destination
     */
    Location getCurrentDestination();

    /**
     * @return A copy of the current path, if any
     */
    Iterable<Vector> getPath();

    /**
     * @return Gets the target destination location
     */
    Location getTargetAsLocation();

    /**
     * @return The {@link TargetType} of this strategy
     */
    TargetType getTargetType();

    /**
     * Forcibly stops pathfinding. Note that this method does not necessarily set the cancel reason.
     */
    void stop();

    /**
     * Updates and runs the pathfinding strategy on its current NPC and destination.
     *
     * @return Whether pathfinding has completed
     */
    boolean update();
}