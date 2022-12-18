package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.npc.NPC.Metadata;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents a storage system for metadata
 */
public interface MetadataStore {
    /**
     * Copies the metadata store.
     */
    MetadataStore clone();

    /**
     * Fetches metadata from the given key.
     *
     * @param key
     *            The key to get metadata from
     * @return The metadata at the given key, or null if not found
     */
    default <T> T get(NPC.Metadata key) {
        return get(key.getKey());
    }

    /**
     * Fetches metadata from the given key.
     *
     * @param key
     *            The key to get metadata from
     * @param def
     *            The default value to return
     * @return The metadata at the given key, or def if not found
     */
    default <T> T get(NPC.Metadata key, T def) {
        return get(key.getKey(), def);
    }

    /**
     * Fetches metadata from the given key.
     *
     * @param key
     *            The key to get metadata from
     * @return The metadata at the given key, or null if not found
     */
    <T> T get(String key);

    /**
     * Fetches metadata from the given key. Sets the default value provided via {@link #set(String, Object)} if the
     * metadata is not already stored.
     *
     * @param key
     *            The key to get metadata from
     * @param def
     *            The default value to return
     * @return The metadata at the given key, or def if not found
     */
    <T> T get(String key, T def);

    /**
     * Returns whether the metadata exists.
     *
     * @param key
     *            The metadata key
     * @return Whether the metadata exists
     */
    default boolean has(NPC.Metadata key) {
        return has(key.getKey());
    }

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
    default void remove(Metadata distance) {
        remove(distance.getKey());
    }

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
    default void set(NPC.Metadata key, Object data) {
        set(key.getKey(), data);
    }

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
     * Stores data at the given key. Data will persist and must be a primitive type or {@link String}.
     *
     * @param key
     *            The metadata key
     * @param data
     *            The data to store
     */
    default void setPersistent(NPC.Metadata key, Object data) {
        setPersistent(key.getKey(), data);
    }

    /**
     * Stores data at the given key. Data will persist and must be a primitive type or {@link String}.
     *
     * @param key
     *            The metadata key
     * @param data
     *            The data to store
     */
    void setPersistent(String key, Object data);

    /**
     *
     * @return The number of elements in the store
     */
    int size();
}
