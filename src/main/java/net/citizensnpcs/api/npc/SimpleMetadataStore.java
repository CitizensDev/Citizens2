package net.citizensnpcs.api.npc;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.util.DataKey;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, MetadataObject> metadata = Maps.newHashMap();

    private void checkPrimitive(Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        boolean isPrimitive = data instanceof String || data instanceof Boolean || data instanceof Number;
        if (!isPrimitive) {
            throw new IllegalArgumentException("data is not primitive");
        }
    }

    @Override
    public <T> T get(NPC.Metadata key) {
        return get(key.getKey());
    }

    @Override
    public <T> T get(NPC.Metadata key, T def) {
        return get(key.getKey(), def);
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
    public boolean has(NPC.Metadata key) {
        return has(key.getKey());
    }

    @Override
    public boolean has(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return metadata.containsKey(key);
    }

    @Override
    public void loadFrom(DataKey key) {
        Iterator<Entry<String, MetadataObject>> itr = metadata.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getValue().persistent) {
                itr.remove();
            }
        }
        for (DataKey subKey : key.getSubKeys()) {
            setPersistent(subKey.name(), subKey.getRaw(""));
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
            if (entry.getValue().persistent) {
                key.setRaw(entry.getKey(), entry.getValue().value);
            }
        }
    }

    @Override
    public void set(NPC.Metadata key, Object data) {
        set(key.getKey(), data);
    }

    @Override
    public void set(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            remove(key);
        } else {
            metadata.put(key, new MetadataObject(data, false));
        }
    }

    @Override
    public void setPersistent(NPC.Metadata key, Object data) {
        setPersistent(key.getKey(), data);
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
            this.value = raw;
            this.persistent = persistent;
        }
    }
}
