package net.citizensnpcs.api.npc;

import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.util.DataKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, MetadataObject> metadata = Maps.newHashMap();

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
        MetadataObject normal = metadata.get(key);
        return normal == null ? null : (T) normal.value;
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
        return metadata.containsKey(key);
    }

    @Override
    public void loadFrom(DataKey key) {
        for (Entry<String, MetadataObject> entry : metadata.entrySet()) {
            if (entry.getValue().persistent)
                metadata.remove(entry.getKey());
        }
        for (DataKey subKey : key.getSubKeys()) {
            metadata.put(subKey.name(), new MetadataObject(subKey.getRaw(""), true));
        }
    }

    @Override
    public void remove(String key) {
        metadata.remove(key);
    }

    @Override
    public void saveTo(DataKey key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        for (Entry<String, MetadataObject> entry : metadata.entrySet()) {
            if (entry.getValue().persistent)
                key.setRaw(entry.getKey(), entry.getValue().value);
        }
    }

    @Override
    public void set(String key, Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        metadata.put(key, new MetadataObject(data, false));
    }

    @Override
    public void setPersistent(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        checkPrimitive(data);
        metadata.put(key, new MetadataObject(data, true));
    }

    private static class MetadataObject {
        final boolean persistent;

        final Object value;

        public MetadataObject(Object raw, boolean persistent) {
            value = raw;
            this.persistent = persistent;
        }
    }
}
