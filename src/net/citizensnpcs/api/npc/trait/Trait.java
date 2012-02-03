package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;

/**
 * Represents a Trait that can be loaded and saved
 */
public interface Trait {

    /**
     * Loads a trait
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             if this trait failed to load properly
     */
    public void load(DataKey key) throws NPCLoadException;

    /**
     * Saves a trait
     * 
     * @param key
     *            DataKey to save to
     */
    public void save(DataKey key);
}