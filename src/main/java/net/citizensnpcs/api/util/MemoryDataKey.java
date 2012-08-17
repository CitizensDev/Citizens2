package net.citizensnpcs.api.util;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class MemoryDataKey extends DataKey {
    private final String path;
    private final ConfigurationSection section;

    public MemoryDataKey() {
        section = new MemoryConfiguration();
        path = "";
    }

    private MemoryDataKey(ConfigurationSection configurationSection, String path) {
        section = configurationSection;
        this.path = path;
    }

    @Override
    public boolean getBoolean(String key) {
        return section.getBoolean(getKeyFor(key), false);
    }

    @Override
    public double getDouble(String key) {
        return section.getDouble(getKeyFor(key), 0D);
    }

    @Override
    public int getInt(String key) {
        return section.getInt(getKeyFor(key), 0);
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
        return section.getLong(getKeyFor(key), 0L);
    }

    @Override
    public Object getRaw(String key) {
        return section.get(getKeyFor(key));
    }

    @Override
    public DataKey getRelative(String relative) {
        ConfigurationSection sub = section.getConfigurationSection(relative);
        if (sub == null)
            sub = section.createSection(relative);
        return new MemoryDataKey(sub, getKeyFor(relative));
    }

    @Override
    public String getString(String key) {
        return section.getString(getKeyFor(key), "");
    }

    @Override
    public Iterable<DataKey> getSubKeys() {
        Set<String> keys = section.getKeys(false);
        return Iterables.transform(keys, new Function<String, DataKey>() {
            @Override
            public DataKey apply(@Nullable String input) {
                ConfigurationSection sub = section.getConfigurationSection(input);
                return sub == null ? null : new MemoryDataKey(sub, getKeyFor(sub.getName()));
            }
        });
    }

    @Override
    public boolean keyExists(String key) {
        return section.isSet(key);
    }

    @Override
    public String name() {
        return section.getName();
    }

    @Override
    public void removeKey(String key) {
        section.set(getKeyFor(key), null);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        section.set(getKeyFor(key), value);
    }

    @Override
    public void setDouble(String key, double value) {
        section.set(getKeyFor(key), value);
    }

    @Override
    public void setInt(String key, int value) {
        section.set(getKeyFor(key), value);
    }

    @Override
    public void setLong(String key, long value) {
        section.set(getKeyFor(key), value);
    }

    @Override
    public void setRaw(String key, Object value) {
        section.set(getKeyFor(key), value);
    }

    @Override
    public void setString(String key, String value) {
        section.set(getKeyFor(key), value);
    }
}
