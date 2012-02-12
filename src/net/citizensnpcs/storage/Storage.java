package net.citizensnpcs.storage;

import net.citizensnpcs.api.DataKey;

public interface Storage {

    public DataKey getKey(String root);

    public void load();

    public void save();
}