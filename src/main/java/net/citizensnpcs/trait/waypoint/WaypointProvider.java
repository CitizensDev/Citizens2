package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Player;

public interface WaypointProvider {

    /**
     * Creates an {@link WaypointEditor} with the given {@link Player}.
     * 
     * @param player
     *            The player to link the editor with
     * @param args
     * @return The editor
     */
    public WaypointEditor createEditor(Player player, CommandContext args);

    /**
     * Returns whether this provider has paused execution of waypoints.
     * 
     * @return Whether the provider is paused.
     */
    public boolean isPaused();

    /**
     * Loads from the specified {@link DataKey}.
     * 
     * @param key
     *            The key to load from
     */
    public void load(DataKey key);

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
     * Saves to the specified {@link DataKey}.
     * 
     * @param key
     *            The key to save to
     */
    public void save(DataKey key);

    /**
     * Pauses waypoint execution.
     * 
     * @param paused
     *            Whether to pause waypoint execution.
     */
    public void setPaused(boolean paused);
}