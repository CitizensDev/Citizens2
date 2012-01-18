package net.citizensnpcs.storage;

import net.citizensnpcs.api.DataKey;

public interface Storage {

	public void load();

	public void save();

	public DataKey getKey(String root);
}