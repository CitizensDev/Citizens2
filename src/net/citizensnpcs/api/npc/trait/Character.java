package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

/**
 * Represents a Character that can be loaded and saved (one Character can be
 * attached to an NPC at a time)
 */
public interface Character {

    /**
     * Loads a trait
     * 
     * @param key
     *            DataKey to load from
     */
    public void load(DataKey key);

    /**
     * Saves a trait
     * 
     * @param key
     *            DataKey to save to
     */
    public void save(DataKey key);

    /**
     * Called when an NPC is left-clicked
     * 
     * @param npc
     *            NPC that was left-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onLeftClick(NPC npc, Player by);

    /**
     * Called when an NPC is right-clicked
     * 
     * @param npc
     *            NPC that was right-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onRightClick(NPC npc, Player by);
}