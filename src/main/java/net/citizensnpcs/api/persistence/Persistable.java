package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

/**
 * A persistable instance that can be saved and loaded using {@link DataKey}
 *
 * @see PersistenceLoader#registerPersistDelegate(Class, Class)
 */
public interface Persistable {
    public void load(DataKey root);

    public void save(DataKey root);
}
