package net.citizensnpcs.api.util;

import java.util.List;

public abstract class DataKey {

    public abstract void copy(String to);

    public abstract boolean getBoolean(String key);

    public boolean getBoolean(String key, boolean value) {
        if (keyExists(key))
            return getBoolean(key);
        setBoolean(key, value);
        return value;
    }

    public abstract double getDouble(String key);

    public double getDouble(String key, double value) {
        if (keyExists(key))
            return getDouble(key);
        setDouble(key, value);
        return value;
    }

    public abstract int getInt(String key);

    public int getInt(String key, int value) {
        if (keyExists(key))
            return getInt(key);
        setInt(key, value);
        return value;
    }

    public abstract List<DataKey> getIntegerSubKeys();

    public abstract long getLong(String key);

    public long getLong(String key, long value) {
        if (keyExists(key))
            return getLong(key);
        setLong(key, value);
        return value;
    }

    public abstract Object getRaw(String key);

    public abstract DataKey getRelative(String relative);

    public abstract String getString(String key);

    public String getString(String key, String value) {
        if (keyExists(key))
            return getString(key);
        setString(key, value);
        return value;
    }

    public abstract Iterable<DataKey> getSubKeys();

    public abstract boolean keyExists(String key);

    public abstract String name();

    public abstract void removeKey(String key);

    public abstract void setBoolean(String key, boolean value);

    public abstract void setDouble(String key, double value);

    public abstract void setInt(String key, int value);

    public abstract void setLong(String key, long value);

    public abstract void setRaw(String path, Object value);

    public abstract void setString(String key, String value);
}