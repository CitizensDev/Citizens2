package net.citizensnpcs.nms.v1_11_R1.util;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.RegistryMaterials;

public class CustomEntityRegistry extends RegistryMaterials {
    private final BiMap<MinecraftKey, Class<? extends Entity>> entities = HashBiMap.create();
    private final BiMap<Class<? extends Entity>, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<Class<? extends Entity>, Integer> entityIds = Maps.newHashMap();
    private final RegistryMaterials<MinecraftKey, Class<? extends Entity>> wrapped;

    public CustomEntityRegistry(RegistryMaterials<MinecraftKey, Class<? extends Entity>> original) {
        this.wrapped = original;
    }

    @Override
    public int a(Object key) {
        if (this.entityIds.containsKey(key)) {
            return this.entityIds.get(key);
        }

        return this.wrapped.a((Class<? extends Entity>) key);
    }

    @Override
    public MinecraftKey b(Object value) {
        if (entityClasses.containsKey(value)) {
            return entityClasses.get(value);
        }

        return wrapped.b((Class<? extends Entity>) value);
    }

    @Override
    public Class<? extends Entity> get(Object key) {
        if (entities.containsKey(key)) {
            return entities.get(key);
        }

        return wrapped.get((MinecraftKey) key);
    }

    public RegistryMaterials<MinecraftKey, Class<? extends Entity>> getWrapped() {
        return wrapped;
    }

    public void put(int entityId, MinecraftKey key, Class<? extends Entity> entityClass) {
        entities.put(key, entityClass);
        entityIds.put(entityClass, entityId);
    }
}