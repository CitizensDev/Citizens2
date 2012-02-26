package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents a Trait that can be loaded and saved
 */
public abstract class Trait {

    /**
     * Loads a trait
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this trait failed to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Saves a trait
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    /**
     * Called when a trait is removed from the given NPC
     * 
     * @param from
     *            NPC this trait was removed from
     */
    public void onRemove(NPC from) {
    }

    /**
     * Gets the name of this trait
     * 
     * @return Name of this trait
     */
    public final String getName() {
        return getClass().getAnnotation(SaveId.class).value();
    }
}