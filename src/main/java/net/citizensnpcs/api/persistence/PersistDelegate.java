package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

public interface PersistDelegate {
    Object create(DataKey root);

    Object save(DataKey root);
}
