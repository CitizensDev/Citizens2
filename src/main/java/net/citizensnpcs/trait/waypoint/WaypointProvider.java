package net.citizensnpcs.trait.waypoint;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persistable;

public interface WaypointProvider extends Persistable {
    /**
     * Creates an {@link WaypointEditor} with the given {@link CommandSender}.
     *
     * @param sender
     *            The player to link the editor with
     * @param args
     * @return The editor
     */
    public WaypointEditor createEditor(CommandSender sender, CommandContext args);

    /**
     * Returns whether this provider has paused execution of waypoints.
     *
     * @return Whether the provider is paused.
     */
    public boolean isPaused();

    /**
     * Called when the provider is removed from the NPC.
     */
    public void onRemove();

    /**
     * Called when the {@link NPC} attached to this provider is spawned.
     *
     * @param npc
     *            The attached NPC
     */
    public void onSpawn(NPC npc);

    /**
     * Pauses waypoint execution.
     *
     * @param paused
     *            Whether to pause waypoint execution.
     */
    public void setPaused(boolean paused);

    public static interface EnumerableWaypointProvider extends WaypointProvider {
        public Iterable<Waypoint> waypoints();
    }
}