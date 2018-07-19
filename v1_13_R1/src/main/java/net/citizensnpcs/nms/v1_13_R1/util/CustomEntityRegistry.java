package net.citizensnpcs.nms.v1_13_R1.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.RegistryMaterials;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends RegistryMaterials {
    private final BiMap<MinecraftKey, EntityTypes> entities = HashBiMap.create();
    private final BiMap<EntityTypes, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<EntityTypes, Integer> entityIds = Maps.newHashMap();
    private final RegistryMaterials<MinecraftKey, EntityTypes<?>> wrapped;

    public CustomEntityRegistry(RegistryMaterials<MinecraftKey, EntityTypes<?>> original) {
        this.wrapped = original;
    }

    @Override
    public void a(int code, Object key, Object v) {
        put(code, (MinecraftKey) key, (EntityTypes) v);
    }

    @Override
    public int a(Object key) {
        if (entityIds.containsKey(key)) {
            return entityIds.get(key);
        }

        return wrapped.a((EntityTypes) key);
    }

    @Override
    public Object a(Random paramRandom) {
        return wrapped.a(paramRandom);
    }

    @Override
    public MinecraftKey b(Object value) {
        if (entityClasses.containsKey(value)) {
            return entityClasses.get(value);
        }

        return wrapped.b((EntityTypes) value);
    }

    @Override
    public boolean d(Object paramK) {
        return wrapped.d((MinecraftKey) paramK);
    }

    public EntityTypes findType(Class<?> search) {
        for (Object type : wrapped) {
            if (((EntityTypes) type).c() == search) {
                return (EntityTypes) type;
            }
        }
        return null;
    }

    @Override
    public EntityTypes get(Object key) {
        if (entities.containsKey(key)) {
            return entities.get(key);
        }

        return wrapped.get((MinecraftKey) key);
    }

    @Override
    public Object getId(int paramInt) {
        return wrapped.getId(paramInt);
    }

    public RegistryMaterials<MinecraftKey, EntityTypes<?>> getWrapped() {
        return wrapped;
    }

    @Override
    public Iterator<Object> iterator() {
        return (Iterator) wrapped.iterator();
    }

    @Override
    public Set<Object> keySet() {
        return (Set) wrapped.keySet();
    }

    public void put(int entityId, MinecraftKey key, EntityTypes entityClass) {
        entities.put(key, entityClass);
        entityIds.put(entityClass, entityId);
    }
}