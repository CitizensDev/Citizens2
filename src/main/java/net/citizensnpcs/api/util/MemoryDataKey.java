package net.citizensnpcs.api.util;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.google.common.collect.Iterables;

public class MemoryDataKey extends DataKey {
    private String name;
    private final ConfigurationSection section;

    public MemoryDataKey() {
        super("");
        section = new MemoryConfiguration();
    }

    MemoryDataKey(ConfigurationSection root, String path) {
        super(path);
        this.section = root;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MemoryDataKey other = (MemoryDataKey) obj;
        return Objects.equals(path, other.path);
    }

    @Override
    public boolean getBoolean(String key) {
        String path = createRelativeKey(key);
        if (valueExists(path)) {
            if (section.getString(path) == null)
                return section.getBoolean(path);
            return Boolean.parseBoolean(section.getString(path));
        }
        return false;
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return (boolean) section.get(createRelativeKey(key), def);
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Override
    public double getDouble(String key, double def) {
        String path = createRelativeKey(key);
        if (valueExists(path)) {
            Object value = section.get(path);
            if (value instanceof Number)
                return ((Number) value).doubleValue();
            String raw = value.toString();
            if (raw.isEmpty())
                return def;
            return Double.parseDouble(raw);
        }
        return def;
    }

    @Override
    public DataKey getFromRoot(String path) {
        return new MemoryDataKey(section.getRoot(), path);
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int def) {
        String path = createRelativeKey(key);
        if (valueExists(path)) {
            Object value = section.get(path);
            if (value instanceof Number)
                return ((Number) value).intValue();
            String raw = value.toString();
            if (raw.isEmpty())
                return def;
            return Integer.parseInt(raw);
        }
        return def;
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long def) {
        String path = createRelativeKey(key);
        if (valueExists(path)) {
            Object value = section.get(path);
            if (value instanceof Number)
                return ((Number) value).longValue();
            String raw = value.toString();
            if (raw.isEmpty())
                return def;
            return Long.parseLong(raw);
        }
        return def;
    }

    @Override
    public Object getRaw(String key) {
        return section.get(createRelativeKey(key));
    }

    @Override
    public MemoryDataKey getRelative(String relative) {
        if (relative == null || relative.isEmpty())
            return this;
        return new MemoryDataKey(section, createRelativeKey(relative));
    }

    public ConfigurationSection getSection(String key) {
        return section.getConfigurationSection(createRelativeKey(key));
    }

    @Override
    public String getString(String key) {
        key = createRelativeKey(key);
        Object val = section.get(key);
        if (val != null && !(val instanceof ConfigurationSection))
            return val.toString();
        return "";
    }

    @Override
    public Iterable<DataKey> getSubKeys() {
        ConfigurationSection head = section.getConfigurationSection(path);
        if (head == null)
            return Collections.emptyList();
        Set<String> keys = head.getKeys(false);
        return Iterables.transform(keys, k -> new MemoryDataKey(section, createRelativeKey(k)));
    }

    @Override
    public Map<String, Object> getValuesDeep() {
        return sectionToValues(section.getConfigurationSection(path));
    }

    @Override
    public int hashCode() {
        return 31 + (path == null ? 0 : path.hashCode());
    }

    @Override
    public boolean keyExists(String key) {
        return section.get(createRelativeKey(key)) != null;
    }

    @Override
    public String name() {
        if (name == null) {
            int idx = path.lastIndexOf('.');
            name = idx == -1 ? path : path.substring(idx + 1);
        }
        return name;
    }

    @Override
    public void removeKey(String key) {
        set(key, null);
    }

    private void set(String key, Object value) {
        section.set(createRelativeKey(key), value);
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

    private boolean valueExists(String key) {
        Object object = section.get(key);
        return object != null && !(object instanceof ConfigurationSection);
    }
}
