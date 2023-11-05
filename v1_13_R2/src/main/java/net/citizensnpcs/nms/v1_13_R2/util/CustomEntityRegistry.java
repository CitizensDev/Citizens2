package net.citizensnpcs.nms.v1_13_R2.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.RegistryMaterials;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends RegistryMaterials implements Supplier<RegistryMaterials<EntityTypes<?>>> {
    private final BiMap<MinecraftKey, EntityTypes> entities = HashBiMap.create();
    private final BiMap<EntityTypes, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<EntityTypes, Integer> entityIds = Maps.newHashMap();
    private final RegistryMaterials<EntityTypes<?>> wrapped;

    public CustomEntityRegistry(RegistryMaterials<EntityTypes<?>> original) {
        this.wrapped = original;
    }

    @Override
    public int a(Object key) {
        if (entityIds.containsKey(key))
            return entityIds.get(key);
        return wrapped.a((EntityTypes) key);
    }

    @Override
    public Object a(Random paramRandom) {
        return wrapped.a(paramRandom);
    }

    @Override
    public boolean c(MinecraftKey paramK) {
        return wrapped.c(paramK);
    }

    public EntityTypes findType(Class<?> search) {
        for (Object type : wrapped) {
            if (((EntityTypes) type).c() == search)
                return (EntityTypes) type;
        }
        return null;
    }

    @Override
    public RegistryMaterials<EntityTypes<?>> get() {
        return wrapped;
    }

    @Override
    public EntityTypes get(MinecraftKey key) {
        if (entities.containsKey(key))
            return entities.get(key);
        return wrapped.get(key);
    }

    @Override
    public MinecraftKey getKey(Object value) {
        if (entityClasses.containsKey(value))
            return entityClasses.get(value);
        return wrapped.getKey((EntityTypes) value);
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