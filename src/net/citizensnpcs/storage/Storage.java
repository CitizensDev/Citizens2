package net.citizensnpcs.storage;

import java.util.Collection;
import java.util.List;

public interface Storage {

	public void load();

	public void save();

	public void removeKey(String key);

	public String getString(String key);

	public String getString(String key, String value);

	public void setString(String key, String value);

	public int getInt(String key);

	public int getInt(String key, int value);

	public void setInt(String key, int value);

	public double getDouble(String key);

	public double getDouble(String key, double value);

	public void setDouble(String key, double value);

	public long getLong(String key);

	public long getLong(String key, long value);

	public void setLong(String key, long value);

	public boolean getBoolean(String key);

	public boolean getBoolean(String key, boolean value);

	public void setBoolean(String key, boolean value);

	public Object getRaw(String string);

	public void setRaw(String path, Object value);

	public boolean keyExists(String path);

	public Collection<String> getKeys(String string);

	public List<Integer> getIntegerKeys(String string);
}