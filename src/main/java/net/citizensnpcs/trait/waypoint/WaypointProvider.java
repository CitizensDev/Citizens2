package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.npc.NPC;
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
     * 
     * @param npc
     *            The attached {@link NPC} the goal is for.
     * @return The {@link Goal} to attach to the NPC.
     */
    public Goal getGoal(NPC npc);

    /**
     * Loads from the specified {@link DataKey}.
     * 
     * @param key
     *            The key to load from
     */
    public void load(DataKey key);

    /**
     * Saves to the specified {@link DataKey}.
     * 
     * @param key
     *            The key to save to
     */
    public void save(DataKey key);
}