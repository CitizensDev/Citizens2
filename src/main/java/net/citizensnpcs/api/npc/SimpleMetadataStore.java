package net.citizensnpcs.api.npc;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.npc.NPC.Metadata;
import net.citizensnpcs.api.util.DataKey;

public class SimpleMetadataStore implements MetadataStore {
    private final Map<String, MetadataObject> metadata = Maps.newHashMap();
    private final Map<NPC.Metadata, MetadataObject> npcMetadata = Maps.newEnumMap(NPC.Metadata.class);

    private void checkPrimitive(Object data) {
        Preconditions.checkNotNull(data, "data cannot be null");
        boolean isPrimitive = data instanceof String || data instanceof Boolean || data instanceof Number;
        if (!isPrimitive) {
            throw new IllegalArgumentException("data is not primitive");
        }
    }

    @Override
    public MetadataStore clone() {
        SimpleMetadataStore copy = new SimpleMetadataStore();
        copy.metadata.putAll(metadata);
        return copy;
    }

    @Override
    public <T> T get(NPC.Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        MetadataObject normal = this.npcMetadata.get(key);
        return normal == null ? null : (T) normal.value;
    }

    @Override
    public <T> T get(NPC.Metadata key, T def) {
        T t = get(key);
        return t == null ? def : t;
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
            return def;
        }
        return t;
    }

    @Override
    public boolean has(NPC.Metadata key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return this.npcMetadata.containsKey(key);
    }

    @Override
    public boolean has(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return metadata.containsKey(key);
    }

    @Override
    public void loadFrom(DataKey key) {
        metadata.entrySet().removeIf(e -> e.getValue().persistent);
        npcMetadata.entrySet().removeIf(e -> e.getValue().persistent);
        for (DataKey sub : key.getSubKeys()) {
            NPC.Metadata meta = Metadata.byKey(sub.name());
            if (meta != null) {
                setPersistent(meta, sub.getRaw(""));
            } else {
                setPersistent(sub.name(), sub.getRaw(""));
            }
        }

    }

    @Override
    public void remove(NPC.Metadata key) {
        npcMetadata.remove(key);
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

        for (Entry<NPC.Metadata, MetadataObject> entry : npcMetadata.entrySet()) {
            if (entry.getValue().persistent) {
                key.setRaw(entry.getKey().getKey(), entry.getValue().value);
            }
        }
    }

    @Override
    public void set(NPC.Metadata key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.npcMetadata.put(key, new MetadataObject(data, false));
        }

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
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.checkPrimitive(data);
            this.npcMetadata.put(key, new MetadataObject(data, true));
        }
    }

    @Override

    public void setPersistent(String key, Object data) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (data == null) {
            this.remove(key);
        } else {
            this.checkPrimitive(data);
            this.metadata.put(key, new MetadataObject(data, true));
        }
    }

    @Override

    public int size() {
        return this.metadata.size() + this.npcMetadata.size();
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
