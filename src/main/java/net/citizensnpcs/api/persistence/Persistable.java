package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

/**
 * An Object that can be serialised using {@link DataKey}s. {@link PersistenceLoader} will call these methods when
 * serialising objects.
 */
public interface Persistable {
    public void load(DataKey root);

    public void save(DataKey root);
}
