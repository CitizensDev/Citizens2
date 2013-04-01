package net.citizensnpcs.api.util;

import java.util.Map;

import net.citizensnpcs.api.util.DatabaseStorage.DatabaseKey;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class DataKey {
    private final boolean database = this instanceof DatabaseKey;
    protected final String path;

    private boolean transferring = false;

    protected DataKey(String path) {
        this.path = path;
    }

    protected String createRelativeKey(String from) {
        transferOld(from);
        if (from.isEmpty())
            return path;
        if (from.charAt(0) == '.')
            return path.isEmpty() ? from.substring(1, from.length()) : path + from;
        return path.isEmpty() ? from : path + '.' + from;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataKey other = (DataKey) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

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

    public Iterable<DataKey> getIntegerSubKeys() {
        return Iterables.filter(getSubKeys(), SIMPLE_INTEGER_FILTER);
    }

    public abstract long getLong(String key);

    public long getLong(String key, long value) {
        if (keyExists(key))
            return getLong(key);
        setLong(key, value);
        return value;
    }

    public String getPath() {
        return path;
    }

    public abstract Object getRaw(String key);

    public DataKey getRelative(int key) {
        return getRelative(Integer.toString(key));
    }

    public abstract DataKey getRelative(String relative);

    public abstract String getString(String key);

    public String getString(String key, String value) {
        if (keyExists(key))
            return getString(key);
        setString(key, value);
        return value;
    }

    public abstract Iterable<DataKey> getSubKeys();

    public abstract Map<String, Object> getValuesDeep();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    public abstract boolean keyExists(String key);

    public abstract String name();

    public abstract void removeKey(String key);

    public abstract void setBoolean(String key, boolean value);

    public abstract void setDouble(String key, double value);

    public abstract void setInt(String key, int value);

    public abstract void setLong(String key, long value);

    public abstract void setRaw(String key, Object value);

    public abstract void setString(String key, String value);

    protected void transferOld(String key) {
        if (database || transferring)
            return;
        transferring = true;
        String repl = key.replace("-", "");
        if (!keyExists(repl)) {
            transferring = false;
            return;
        }
        Object value = getRaw(repl);
        removeKey(repl);
        setRaw(key, value);
        transferring = false;
    }

    private static final Predicate<DataKey> SIMPLE_INTEGER_FILTER = new Predicate<DataKey>() {
        @Override
        public boolean apply(DataKey key) {
            try {
                Integer.parseInt(key.name());
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    };
}