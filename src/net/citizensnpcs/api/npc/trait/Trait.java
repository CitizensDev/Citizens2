package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;

/**
 * Represents a Trait with a unique name that can be loaded and saved
 */
public interface Trait {

    /**
     * Gets the unique name of this trait
     * 
     * @return Name of the trait
     */
    public String getName();

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