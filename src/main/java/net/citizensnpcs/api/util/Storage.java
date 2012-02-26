package net.citizensnpcs.api.util;


public interface Storage {

    public DataKey getKey(String root);

    public void load();

    public void save();
}