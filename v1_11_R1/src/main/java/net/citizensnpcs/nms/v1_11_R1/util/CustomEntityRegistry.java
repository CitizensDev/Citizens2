package net.citizensnpcs.nms.v1_11_R1.util;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.RegistryMaterials;

public class CustomEntityRegistry extends RegistryMaterials<MinecraftKey, Class<? extends Entity>> {
    private final BiMap<MinecraftKey, Class<? extends Entity>> entities = HashBiMap.create();
    private final BiMap<Class<? extends Entity>, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<Class<? extends Entity>, Integer> entityIds = Maps.newHashMap();
    private final RegistryMaterials<MinecraftKey, Class<? extends Entity>> wrapped;

    public CustomEntityRegistry(RegistryMaterials<MinecraftKey, Class<? extends Entity>> original) {
        this.wrapped = original;
    }

    @Override
    public int a(Class<? extends Entity> key) {
        if (this.entityIds.containsKey(key)) {
            return this.entityIds.get(key);
        }

        return this.wrapped.a(key);
    }

    @Override
    public MinecraftKey b(Class<? extends Entity> value) {
        if (entityClasses.containsKey(value)) {
            return entityClasses.get(value);
        }

        return wrapped.b(value);
    }

    @Override
    public Class<? extends Entity> get(MinecraftKey key) {
        if (entities.containsKey(key)) {
            return entities.get(key);
        }

        return wrapped.get(key);
    }

    public RegistryMaterials<MinecraftKey, Class<? extends Entity>> getWrapped() {
        return wrapped;
    }

    public void put(int entityId, MinecraftKey key, Class<? extends Entity> entityClass) {
        this.entities.put(key, entityClass);
        this.entityIds.put(entityClass, entityId);
    }
}