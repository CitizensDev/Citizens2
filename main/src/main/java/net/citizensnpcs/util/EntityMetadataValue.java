package net.citizensnpcs.util;

public class EntityMetadataValue {
    public int id;
    public int serializerId;
    public Object value;

    public EntityMetadataValue(int id, int serializerId, Object value) {
        this.id = id;
        this.serializerId = serializerId;
        this.value = value;
    }
}
