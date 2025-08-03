package net.citizensnpcs.nms.v1_11_R1.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.RegistryMaterials;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends RegistryMaterials
        implements Supplier<RegistryMaterials<MinecraftKey, Class<? extends Entity>>> {
    private final BiMap<MinecraftKey, Class<? extends Entity>> entities = HashBiMap.create();
    private final BiMap<Class<? extends Entity>, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<Class<? extends Entity>, Integer> entityIds = Maps.newHashMap();
    private final RegistryMaterials<MinecraftKey, Class<? extends Entity>> wrapped;

    public CustomEntityRegistry(RegistryMaterials<MinecraftKey, Class<? extends Entity>> original) {
        this.wrapped = original;
    }

    @Override
    public void a(int code, Object key, Object v) {
        put(code, (MinecraftKey) key, (Class<? extends Entity>) v);
    }

    @Override
    public int a(Object key) {
        if (entityIds.containsKey(key))
            return entityIds.get(key);
        return wrapped.a((Class<? extends Entity>) key);
    }

    @Override
    public Object a(Random paramRandom) {
        return wrapped.a(paramRandom);
    }

    @Override
    public MinecraftKey b(Object value) {
        if (entityClasses.containsKey(value))
            return entityClasses.get(value);
        return wrapped.b((Class<? extends Entity>) value);
    }

    @Override
    public boolean d(Object paramK) {
        return wrapped.d((MinecraftKey) paramK);
    }

    @Override
    public RegistryMaterials<MinecraftKey, Class<? extends Entity>> get() {
        return wrapped;
    }

    @Override
    public Class<? extends Entity> get(Object key) {
        if (entities.containsKey(key))
            return entities.get(key);
        return wrapped.get((MinecraftKey) key);
    }

    @Override
    public Object getId(int paramInt) {
        return wrapped.getId(paramInt);
    }

    @Override
    public Iterator<Object> iterator() {
        return (Iterator) wrapped.iterator();
    }

    @Override
    public Set<Object> keySet() {
        return (Set) wrapped.keySet();
    }

    public void put(int entityId, MinecraftKey key, Class<? extends Entity> entityClass) {
        entities.put(key, entityClass);
        entityIds.put(entityClass, entityId);
    }
}