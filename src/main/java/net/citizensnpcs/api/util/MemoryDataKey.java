package net.citizensnpcs.api.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.google.common.collect.Iterables;

public class MemoryDataKey extends DataKey {
    private String name;
    private final ConfigurationSection root;

    public MemoryDataKey() {
        super("");
        root = new MemoryConfiguration();
    }

    private MemoryDataKey(ConfigurationSection root, String path) {
        super(path);
        this.root = root;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemoryDataKey other = (MemoryDataKey) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean getBoolean(String key) {
        return root.getBoolean(getKeyFor(key), false);
    }

    @Override
    public double getDouble(String key) {
        return root.getDouble(getKeyFor(key), 0D);
    }

    @Override
    public DataKey getFromRoot(String path) {
        return new MemoryDataKey(root, path);
    }

    @Override
    public int getInt(String key) {
        return root.getInt(getKeyFor(key), 0);
    }

    private String getKeyFor(String key) {
        if (key.isEmpty())
            return path;
        if (key.charAt(0) == '.')
            return path.isEmpty() ? key.substring(1, key.length()) : path + key;
        return path.isEmpty() ? key : path + "." + key;
    }

    @Override
    public long getLong(String key) {
        return root.getLong(getKeyFor(key), 0L);
    }

    @Override
    public Object getRaw(String key) {
        return root.get(getKeyFor(key));
    }

    @Override
    public MemoryDataKey getRelative(String relative) {
        String key = getKeyFor(relative);
        return new MemoryDataKey(root, key);
    }

    @Override
    public String getString(String key) {
        return root.getString(getKeyFor(key), "");
    }

    @Override
    public Iterable<DataKey> getSubKeys() {
        ConfigurationSection head = root.getConfigurationSection(path);
        if (head == null)
            return Collections.emptyList();
        Set<String> keys = head.getKeys(false);
        return Iterables.transform(keys, input -> new MemoryDataKey(root, getKeyFor(input)));
    }

    @Override
    public Map<String, Object> getValuesDeep() {
        return sectionToValues(root);
    }

    @Override
    public int hashCode() {
        return 31 + ((path == null) ? 0 : path.hashCode());
    }

    @Override
    public boolean keyExists(String key) {
        return root.isSet(getKeyFor(key));
    }

    @Override
    public String name() {
        if (name == null) {
            int idx = path.lastIndexOf('.');
            name = idx == -1 ? path : path.substring(idx + 1, path.length());
        }
        return name;
    }

    @Override
    public void removeKey(String key) {
        set(key, null);
    }

    private void set(String key, Object value) {
        root.set(getKeyFor(key), value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        set(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        set(key, value);
    }

    @Override
    public void setInt(String key, int value) {
        set(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        set(key, value);
    }

    @Override
    public void setRaw(String key, Object value) {
        set(key, value);
    }

    @Override
    public void setString(String key, String value) {
        set(key, value);
    }

    @Override
    public String toString() {
        return "MemoryDataKey[" + path + "]";
    }
}
