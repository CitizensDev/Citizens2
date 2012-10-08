package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

public interface Persister {
    Object create(DataKey root);

    void save(Object instance, DataKey root);
}
