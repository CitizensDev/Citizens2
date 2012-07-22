package net.citizensnpcs.api.npc;

import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.util.DataKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, Object> normalMetadata = Maps.newHashMap();
    private final Map<String, Object> persistentMetadata = Maps.newHashMap();

    private void checkPrimitive(Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        boolean isPrimitive = data instanceof String || data instanceof Boolean || data instanceof Number;
        if (!isPrimitive)
            throw new IllegalArgumentException("data is not primitive");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Object normal = normalMetadata.get(key);
        if (normal != null)
            return (T) normal;
        return (T) persistentMetadata.get(key);
    }

    @Override
    public <T> T get(String key, T def) {
        T t = get(key);
        if (t == null) {
            set(key, def);
            return def;
        }
        return t;
    }

    @Override
    public boolean has(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return normalMetadata.containsKey(key) || persistentMetadata.containsKey(key);
    }

    @Override
    public void loadFrom(DataKey key) {
        persistentMetadata.clear();
        for (DataKey subKey : key.getSubKeys()) {
            persistentMetadata.put(subKey.name(), subKey.getRaw(""));
        }
    }

    @Override
    public void remove(String key) {
        normalMetadata.remove(key);
        persistentMetadata.remove(key);
    }

    @Override
    public void saveTo(DataKey key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        for (Entry<String, Object> entry : persistentMetadata.entrySet()) {
            key.setRaw(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void set(String key, Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        if (persistentMetadata.containsKey(key))
            throw new IllegalArgumentException("conflicting persistent key");
        normalMetadata.put(key, data);
    }

    @Override
    public void setPersistent(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        checkPrimitive(data);
        if (normalMetadata.containsKey(key))
            throw new IllegalArgumentException("conflicting normal key");
        persistentMetadata.put(key, data);
    }
}
