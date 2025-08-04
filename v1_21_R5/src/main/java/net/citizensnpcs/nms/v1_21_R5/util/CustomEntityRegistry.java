package net.citizensnpcs.nms.v1_21_R5.util;

import java.lang.invoke.MethodHandle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import net.citizensnpcs.util.NMS;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.IdMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader.LoadResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends DefaultedMappedRegistry<EntityType<?>>
        implements Supplier<MappedRegistry<EntityType<?>>> {
    private final Reference<EntityType<?>> defaultReference;
    private final BiMap<ResourceLocation, Reference<EntityType<?>>> entities = HashBiMap.create();
    private final BiMap<Reference<EntityType<?>>, ResourceLocation> entityClasses = this.entities.inverse();
    private final Map<EntityType, Integer> entityIds = Maps.newHashMap();
    private final MappedRegistry<EntityType<?>> wrapped;

    public CustomEntityRegistry(DefaultedRegistry<EntityType<?>> original) throws Throwable {
        super(original.getDefaultKey().getNamespace(),
                (ResourceKey<? extends Registry<EntityType<?>>>) IREGISTRY_RESOURCE_KEY.invoke(original),
                (Lifecycle) IREGISTRY_LIFECYCLE.invoke(original), true);
        defaultReference = EntityType.PIG.builtInRegistryHolder();
        this.wrapped = (MappedRegistry<EntityType<?>>) original;
    }

    @Override
    public IdMap<Holder<EntityType<?>>> asHolderIdMap() {
        return wrapped.asHolderIdMap();
    }

    @Override
    public void bindAllTagsToEmpty() {
        wrapped.bindAllTagsToEmpty();
    }

    @Override
    public void bindTag(TagKey<EntityType<?>> tag, List<Holder<EntityType<?>>> list) {
        wrapped.bindTag(tag, list);
    }

    @Override
    public EntityType byId(int var0) {
        return wrapped.byId(var0);
    }

    @Override
    public EntityType byIdOrThrow(int var0) {
        return wrapped.byIdOrThrow(var0);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<EntityType<?>> owner) {
        return wrapped.canSerializeIn(owner);
    }

    @Override
    public boolean containsKey(ResourceKey<EntityType<?>> var0) {
        return wrapped.containsKey(var0);
    }

    @Override
    public boolean containsKey(ResourceLocation var0) {
        return wrapped.containsKey(var0);
    }

    @Override
    public Reference<EntityType<?>> createIntrusiveHolder(EntityType<?> type) {
        return wrapped.createIntrusiveHolder(type);
    }

    @Override
    public HolderGetter<EntityType<?>> createRegistrationLookup() {
        return wrapped.createRegistrationLookup();
    }

    @Override
    public Set<Entry<ResourceKey<EntityType<?>>, EntityType<?>>> entrySet() {
        return wrapped.entrySet();
    }

    @Override
    public Registry<EntityType<?>> freeze() {
        return wrapped.freeze();
    }

    @Override
    public MappedRegistry<EntityType<?>> get() {
        return wrapped;
    }

    @Override
    public Optional<Reference<EntityType<?>>> get(ResourceKey<EntityType<?>> key) {
        return wrapped.get(key);
    }

    @Override
    public Optional<Reference<EntityType<?>>> get(ResourceLocation key) {
        if (entities.containsKey(key))
            return Optional.ofNullable(entities.get(key));
        return wrapped.get(key);
    }

    @Override
    public Optional<Named<EntityType<?>>> get(TagKey<EntityType<?>> key) {
        return wrapped.get(key);
    }

    @Override
    public Optional<Reference<EntityType<?>>> getAny() {
        return wrapped.getAny();
    }

    @Override
    public int getId(EntityType<?> key) {
        if (entityIds.containsKey(key))
            return entityIds.get(key);
        return wrapped.getId(key);
    }

    @Override
    public ResourceLocation getKey(EntityType<?> value) {
        return wrapped.getKey(value);
    }

    @Override
    public Optional<EntityType<?>> getOptional(ResourceKey<EntityType<?>> var0) {
        return this.wrapped.getOptional(var0);
    }

    @Override
    public Optional<EntityType<?>> getOptional(ResourceLocation var0) {
        if (entities.containsKey(var0))
            return Optional.ofNullable(getValue(var0));
        return this.wrapped.getOptional(var0);
    }

    @Override
    public Optional<Reference<EntityType<?>>> getRandom(RandomSource paramRandom) {
        return wrapped.getRandom(paramRandom);
    }

    @Override
    public Optional<ResourceKey<EntityType<?>>> getResourceKey(EntityType<?> var0) {
        return wrapped.getResourceKey(var0);
    }

    @Override
    public Stream<Named<EntityType<?>>> getTags() {
        return wrapped.getTags();
    }

    @Override
    public EntityType<?> getValue(ResourceKey<EntityType<?>> key) {
        return wrapped.getValue(key);
    }

    @Override
    public EntityType<?> getValue(ResourceLocation key) {
        if (entities.containsKey(key))
            return entities.getOrDefault(key, defaultReference).value();
        return wrapped.getValue(key);
    }

    @Override
    public Codec<Holder<EntityType<?>>> holderByNameCodec() {
        return wrapped.holderByNameCodec();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public Iterator<EntityType<?>> iterator() {
        return wrapped.iterator();
    }

    @Override
    public ResourceKey<? extends Registry<EntityType<?>>> key() {
        return wrapped.key();
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return wrapped.keySet();
    }

    @Override
    public Stream<Reference<EntityType<?>>> listElements() {
        return wrapped.listElements();
    }

    @Override
    public Stream<Named<EntityType<?>>> listTags() {
        return wrapped.listTags();
    }

    @Override
    public PendingTags<EntityType<?>> prepareTagReload(LoadResult<EntityType<?>> var0) {
        return wrapped.prepareTagReload(var0);
    }

    public void put(int entityId, ResourceLocation key, EntityType entityClass) {
        entities.put(key, entityClass.builtInRegistryHolder());
        entityIds.put(entityClass, entityId);
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<EntityType<?>> key) {
        return wrapped.registrationInfo(key);
    }

    @Override
    public Set<ResourceKey<EntityType<?>>> registryKeySet() {
        return wrapped.registryKeySet();
    }

    @Override
    public Lifecycle registryLifecycle() {
        return wrapped.registryLifecycle();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    @Override
    public Holder<EntityType<?>> wrapAsHolder(EntityType<?> type) {
        return wrapped.wrapAsHolder(type);
    }

    private static final MethodHandle IREGISTRY_LIFECYCLE = NMS.getFirstGetter(MappedRegistry.class, Lifecycle.class);
    private static final MethodHandle IREGISTRY_RESOURCE_KEY = NMS.getFirstGetter(MappedRegistry.class,
            ResourceKey.class);
}