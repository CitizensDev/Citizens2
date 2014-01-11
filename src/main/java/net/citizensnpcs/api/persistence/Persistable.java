package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

public interface Persistable {
    public void load(DataKey root);

    public void save(DataKey root);
}
