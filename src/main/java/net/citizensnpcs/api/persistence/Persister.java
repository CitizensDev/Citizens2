package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

public interface Persister<T> {
    /**
     * Creates an object instance from the given {@link DataKey}. Should not return null unless no data is present.
     * 
     * @param root
     *            The root key to load from
     * @return The created instance, or null if no data was present
     */
    T create(DataKey root);

    /**
     * Saves the object instance to the given {@link DataKey}.
     * 
     * @param instance
     *            The object instance to save
     * @param root
     *            The key to save into
     */
    void save(T instance, DataKey root);
}
