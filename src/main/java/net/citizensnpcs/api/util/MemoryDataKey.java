package net.citizensnpcs.api.util;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class MemoryDataKey extends DataKey {
    private final ConfigurationSection section;

    public MemoryDataKey() {
        section = new MemoryConfiguration();
    }

    private MemoryDataKey(ConfigurationSection configurationSection) {
        section = configurationSection;
    }

    @Override
    public boolean getBoolean(String key) {
        return section.getBoolean(key);
    }

    @Override
    public double getDouble(String key) {
        return section.getDouble(key);
    }

    @Override
    public int getInt(String key) {
        return section.getInt(key);
    }

    @Override
    public long getLong(String key) {
        return section.getLong(key);
    }

    @Override
    public Object getRaw(String key) {
        return section.get(key);
    }

    @Override
    public DataKey getRelative(String relative) {
        ConfigurationSection sub = section.getConfigurationSection(relative);
        if (sub == null)
            section.createSection(relative);
        return new MemoryDataKey(sub);
    }

    @Override
    public String getString(String key) {
        return section.getString(key);
    }

    @Override
    public Iterable<DataKey> getSubKeys() {
        Set<String> keys = section.getKeys(false);
        return Iterables.transform(keys, new Function<String, DataKey>() {
            @Override
            public DataKey apply(@Nullable String input) {
                ConfigurationSection sub = section.getConfigurationSection(input);
                return sub == null ? null : new MemoryDataKey(sub);
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
        section.set(key, null);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        section.set(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        section.set(key, value);
    }

    @Override
    public void setInt(String key, int value) {
        section.set(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        section.set(key, value);
    }

    @Override
    public void setRaw(String key, Object value) {
        section.set(key, value);
    }

    @Override
    public void setString(String key, String value) {
        section.set(key, value);
    }
}
