package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public interface WaypointProvider {

    /**
     * Creates an {@link Editor} with the given {@link Player}.
     * 
     * @param player
     *            The player to link the editor with
     * @return The editor
     */
    public Editor createEditor(Player player);

    /**
     * Returns the {@link NavigationCallback} linked to this provider. This will
     * be linked to the NPC's {@link AI}.
     * 
     * @return The callback in use
     */
    public NavigationCallback getCallback();

    /**
     * Loads from the specified {@link DataKey}.
     * 
     * @param key
     *            The key to load from
     */
    public void load(DataKey key);

    public void onAttach();

    /**
     * Saves to the specified {@link DataKey}.
     * 
     * @param key
     *            The key to save to
     */
    public void save(DataKey key);
}