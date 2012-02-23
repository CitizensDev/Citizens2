package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Player;

/**
 * Represents a Character that can be loaded and saved (one Character can be
 * attached to an NPC at a time)
 */
public abstract class Character {

    /**
     * Loads a trait
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this character fails to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Called when an NPC is left-clicked
     * 
     * @param npc
     *            NPC that was left-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onLeftClick(NPC npc, Player by) {
    }

    /**
     * Called when an NPC is right-clicked
     * 
     * @param npc
     *            NPC that was right-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onRightClick(NPC npc, Player by) {
    }

    /**
     * Saves a trait
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    /**
     * Gets the name of this character
     * 
     * @return Name of this character
     */
    public final String getName() {
        return getClass().getAnnotation(SaveId.class).value();
    }

    /**
     * Called when an NPC is set as this character
     * 
     * @param npc
     *            NPC that is set as this character
     */
    public void onSet(NPC npc) {
    }

    /**
     * Called when this character is removed from an NPC
     * 
     * @param npc
     *            NPC that had this character removed
     */
    public void onRemove(NPC npc) {
    }
}