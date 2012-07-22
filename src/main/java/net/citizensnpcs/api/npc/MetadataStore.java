package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.util.DataKey;

/**
 * Represents a metadata
 */
public interface MetadataStore {
    /**
     * Fetches metadata from the given key.
     * 
     * @param key
     *            The key to get metadata from
     * @return The metadata at the given key, or null if not found
     */
    <T> T get(String key);

/**
     *  Fetches metadata from the given key. Sets the default value provided via
     * {@link #set(String, Object) if the metadata is not already stored.
     * 
     * @param key
     *            The key to get metadata from
     * @param def
     *            The default value to return
     * @return The metadata at the given key, or def if not found
     */
    <T> T get(String string, T def);

    /**
     * Returns whether the metadata exists.
     * 
     * @param key
     *            The metadata key
     * @return Whether the metadata exists
     */
    boolean has(String key);

    /**
     * Loads persistent metadata from the given {@link DataKey}.
     * 
     * @param key
     *            The key to load from
     */
    void loadFrom(DataKey key);

    /**
     * Removes any metadata at the given metadata key.
     * 
     * @param key
     *            The metadata key
     */
    void remove(String key);

    /**
     * Saves persistent metadata to the given {@link DataKey}.
     * 
     * @param key
     *            The key to save to.
     */
    void saveTo(DataKey key);

    /**
     * Stores data at the given key. Data will not persist.
     * 
     * @param key
     *            The metadata key
     * @param data
     *            The data to store
     */
    void set(String key, Object data);

    /**
     * Stores data at the given key. Data will persist and must be a primitive
     * type or {@link String}.
     * 
     * @param key
     *            The metadata key
     * @param data
     *            The data to store
     */
    void setPersistent(String key, Object data);
}
