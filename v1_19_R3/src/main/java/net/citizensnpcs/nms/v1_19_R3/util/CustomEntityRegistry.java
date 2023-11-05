package net.citizensnpcs.nms.v1_19_R3.util;

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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import net.citizensnpcs.util.NMS;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends DefaultedMappedRegistry<EntityType<?>>
        implements Supplier<MappedRegistry<EntityType<?>>> {
    private final BiMap<ResourceLocation, EntityType> entities = HashBiMap.create();
    private final BiMap<EntityType, ResourceLocation> entityClasses = this.entities.inverse();
    private final Map<EntityType, Integer> entityIds = Maps.newHashMap();
    private final MappedRegistry<EntityType<?>> wrapped;

    public CustomEntityRegistry(DefaultedRegistry<EntityType<?>> original) throws Throwable {
        super(original.getDefaultKey().getNamespace(),
                (ResourceKey<? extends Registry<EntityType<?>>>) IREGISTRY_RESOURCE_KEY.invoke(original),
                (Lifecycle) IREGISTRY_LIFECYCLE.invoke(original), true);
        this.wrapped = (MappedRegistry<EntityType<?>>) original;
    }

    @Override
    public RegistryLookup<EntityType<?>> asLookup() {
        return wrapped.asLookup();
    }

    @Override
    public void bindTags(Map<TagKey<EntityType<?>>, List<Holder<EntityType<?>>>> map) {
        wrapped.bindTags(map);
    }

    @Override
    public EntityType byId(int var0) {
        return this.wrapped.byId(var0);
    }

    @Override
    public EntityType byIdOrThrow(int var0) {
        return this.wrapped.byIdOrThrow(var0);
    }

    @Override
    public boolean containsKey(ResourceKey<EntityType<?>> var0) {
        return this.wrapped.containsKey(var0);
    }

    @Override
    public boolean containsKey(ResourceLocation var0) {
        return this.wrapped.containsKey(var0);
    }

    @Override
    public HolderGetter<EntityType<?>> createRegistrationLookup() {
        return wrapped.createRegistrationLookup();
    }

    @Override
    public Set<Entry<ResourceKey<EntityType<?>>, EntityType<?>>> entrySet() {
        return wrapped.entrySet();
    }

    public EntityType findType(Class<?> search) {
        return minecraftClassMap.inverse().get(search);
    }

    @Override
    public MappedRegistry<EntityType<?>> get() {
        return wrapped;
    }

    @Override
    public EntityType get(ResourceKey<EntityType<?>> key) {
        return wrapped.get(key);
    }

    @Override
    public EntityType get(ResourceLocation key) {
        if (entities.containsKey(key))
            return entities.get(key);
        return wrapped.get(key);
    }

    @Override
    public Optional<Reference<EntityType<?>>> getHolder(int var0) {
        return this.wrapped.getHolder(var0);
    }

    @Override
    public Optional<Reference<EntityType<?>>> getHolder(ResourceKey<EntityType<?>> var0) {
        return this.wrapped.getHolder(var0);
    }

    @Override
    public int getId(EntityType key) {
        if (entityIds.containsKey(key))
            return entityIds.get(key);
        return wrapped.getId(key);
    }

    @Override
    public ResourceLocation getKey(EntityType value) {
        if (entityClasses.containsKey(value))
            return entityClasses.get(value);
        return wrapped.getKey(value);
    }

    @Override
    public Optional<EntityType<?>> getOptional(ResourceKey<EntityType<?>> var0) {
        return this.wrapped.getOptional(var0);
    }

    @Override
    public Optional<EntityType<?>> getOptional(ResourceLocation var0) {
        if (entities.containsKey(var0))
            return Optional.of(entities.get(var0));
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
    public Optional<Named<EntityType<?>>> getTag(TagKey<EntityType<?>> var0) {
        return this.wrapped.getTag(var0);
    }

    @Override
    public Stream<TagKey<EntityType<?>>> getTagNames() {
        return wrapped.getTagNames();
    }

    @Override
    public Stream<Pair<TagKey<EntityType<?>>, Named<EntityType<?>>>> getTags() {
        return wrapped.getTags();
    }

    @Override
    public HolderOwner<EntityType<?>> holderOwner() {
        return wrapped.holderOwner();
    }

    @Override
    public Stream<Reference<EntityType<?>>> holders() {
        return wrapped.holders();
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
    public Set<ResourceLocation> keySet() {
        return wrapped.keySet();
    }

    @Override
    public Lifecycle lifecycle(EntityType type) {
        return wrapped.lifecycle(type);
    }

    public void put(int entityId, ResourceLocation key, EntityType entityClass) {
        entities.put(key, entityClass);
        entityIds.put(entityClass, entityId);
    }

    @Override
    public Set<ResourceKey<EntityType<?>>> registryKeySet() {
        return wrapped.registryKeySet();
    }

    @Override
    public void resetTags() {
        wrapped.resetTags();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    private static final MethodHandle IREGISTRY_LIFECYCLE = NMS.getFirstGetter(MappedRegistry.class, Lifecycle.class);
    // replace regex .*?> ([A-Z_]+).*?of\((.*?)::new.*?$ minecraftClassMap.put(EntityType.\1, \2.class);
    private static final MethodHandle IREGISTRY_RESOURCE_KEY = NMS.getFirstGetter(MappedRegistry.class,
            ResourceKey.class);
    private static final BiMap<EntityType, Class<?>> minecraftClassMap = HashBiMap.create();
    static {
        minecraftClassMap.put(EntityType.ALLAY, Allay.class);
        minecraftClassMap.put(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloud.class);
        minecraftClassMap.put(EntityType.ARMOR_STAND, ArmorStand.class);
        minecraftClassMap.put(EntityType.ARROW, Arrow.class);
        minecraftClassMap.put(EntityType.AXOLOTL, Axolotl.class);
        minecraftClassMap.put(EntityType.BAT, Bat.class);
        minecraftClassMap.put(EntityType.BEE, Bee.class);
        minecraftClassMap.put(EntityType.BLAZE, Blaze.class);
        minecraftClassMap.put(EntityType.BOAT, Boat.class);
        minecraftClassMap.put(EntityType.CHEST_BOAT, ChestBoat.class);
        minecraftClassMap.put(EntityType.CAT, Cat.class);
        minecraftClassMap.put(EntityType.CAMEL, Camel.class);
        minecraftClassMap.put(EntityType.CAVE_SPIDER, CaveSpider.class);
        minecraftClassMap.put(EntityType.CHICKEN, Chicken.class);
        minecraftClassMap.put(EntityType.COD, Cod.class);
        minecraftClassMap.put(EntityType.COW, Cow.class);
        minecraftClassMap.put(EntityType.CREEPER, Creeper.class);
        minecraftClassMap.put(EntityType.DOLPHIN, Dolphin.class);
        minecraftClassMap.put(EntityType.DONKEY, Donkey.class);
        minecraftClassMap.put(EntityType.DRAGON_FIREBALL, DragonFireball.class);
        minecraftClassMap.put(EntityType.DROWNED, Drowned.class);
        minecraftClassMap.put(EntityType.ELDER_GUARDIAN, ElderGuardian.class);
        minecraftClassMap.put(EntityType.END_CRYSTAL, EndCrystal.class);
        minecraftClassMap.put(EntityType.ENDER_DRAGON, EnderDragon.class);
        minecraftClassMap.put(EntityType.ENDERMAN, EnderMan.class);
        minecraftClassMap.put(EntityType.ENDERMITE, Endermite.class);
        minecraftClassMap.put(EntityType.EVOKER, Evoker.class);
        minecraftClassMap.put(EntityType.EVOKER_FANGS, EvokerFangs.class);
        minecraftClassMap.put(EntityType.EXPERIENCE_ORB, ExperienceOrb.class);
        minecraftClassMap.put(EntityType.EYE_OF_ENDER, EyeOfEnder.class);
        minecraftClassMap.put(EntityType.FALLING_BLOCK, FallingBlockEntity.class);
        minecraftClassMap.put(EntityType.FIREWORK_ROCKET, FireworkRocketEntity.class);
        minecraftClassMap.put(EntityType.FOX, Fox.class);
        minecraftClassMap.put(EntityType.FROG, Frog.class);
        minecraftClassMap.put(EntityType.GHAST, Ghast.class);
        minecraftClassMap.put(EntityType.GIANT, Giant.class);
        minecraftClassMap.put(EntityType.GLOW_ITEM_FRAME, GlowItemFrame.class);
        minecraftClassMap.put(EntityType.GLOW_SQUID, GlowSquid.class);
        minecraftClassMap.put(EntityType.GOAT, Goat.class);
        minecraftClassMap.put(EntityType.GUARDIAN, Guardian.class);
        minecraftClassMap.put(EntityType.HOGLIN, Hoglin.class);
        minecraftClassMap.put(EntityType.HORSE, Horse.class);
        minecraftClassMap.put(EntityType.HUSK, Husk.class);
        minecraftClassMap.put(EntityType.ILLUSIONER, Illusioner.class);
        minecraftClassMap.put(EntityType.IRON_GOLEM, IronGolem.class);
        minecraftClassMap.put(EntityType.ITEM, ItemEntity.class);
        minecraftClassMap.put(EntityType.ITEM_FRAME, ItemFrame.class);
        minecraftClassMap.put(EntityType.FIREBALL, LargeFireball.class);
        minecraftClassMap.put(EntityType.LEASH_KNOT, LeashFenceKnotEntity.class);
        minecraftClassMap.put(EntityType.LIGHTNING_BOLT, LightningBolt.class);
        minecraftClassMap.put(EntityType.LLAMA, Llama.class);
        minecraftClassMap.put(EntityType.LLAMA_SPIT, LlamaSpit.class);
        minecraftClassMap.put(EntityType.MAGMA_CUBE, MagmaCube.class);
        minecraftClassMap.put(EntityType.MARKER, Marker.class);
        minecraftClassMap.put(EntityType.MINECART, Minecart.class);
        minecraftClassMap.put(EntityType.CHEST_MINECART, MinecartChest.class);
        minecraftClassMap.put(EntityType.COMMAND_BLOCK_MINECART, MinecartCommandBlock.class);
        minecraftClassMap.put(EntityType.FURNACE_MINECART, MinecartFurnace.class);
        minecraftClassMap.put(EntityType.HOPPER_MINECART, MinecartHopper.class);
        minecraftClassMap.put(EntityType.SPAWNER_MINECART, MinecartSpawner.class);
        minecraftClassMap.put(EntityType.TNT_MINECART, MinecartTNT.class);
        minecraftClassMap.put(EntityType.MULE, Mule.class);
        minecraftClassMap.put(EntityType.MOOSHROOM, MushroomCow.class);
        minecraftClassMap.put(EntityType.OCELOT, Ocelot.class);
        minecraftClassMap.put(EntityType.PAINTING, Painting.class);
        minecraftClassMap.put(EntityType.PANDA, Panda.class);
        minecraftClassMap.put(EntityType.PARROT, Parrot.class);
        minecraftClassMap.put(EntityType.PHANTOM, Phantom.class);
        minecraftClassMap.put(EntityType.PIG, Pig.class);
        minecraftClassMap.put(EntityType.PIGLIN, Piglin.class);
        minecraftClassMap.put(EntityType.PIGLIN_BRUTE, PiglinBrute.class);
        minecraftClassMap.put(EntityType.PILLAGER, Pillager.class);
        minecraftClassMap.put(EntityType.POLAR_BEAR, PolarBear.class);
        minecraftClassMap.put(EntityType.TNT, PrimedTnt.class);
        minecraftClassMap.put(EntityType.PUFFERFISH, Pufferfish.class);
        minecraftClassMap.put(EntityType.RABBIT, Rabbit.class);
        minecraftClassMap.put(EntityType.RAVAGER, Ravager.class);
        minecraftClassMap.put(EntityType.SALMON, Salmon.class);
        minecraftClassMap.put(EntityType.SHEEP, Sheep.class);
        minecraftClassMap.put(EntityType.SHULKER, Shulker.class);
        minecraftClassMap.put(EntityType.SHULKER_BULLET, ShulkerBullet.class);
        minecraftClassMap.put(EntityType.SNIFFER, net.minecraft.world.entity.animal.sniffer.Sniffer.class);
        minecraftClassMap.put(EntityType.BLOCK_DISPLAY, Display.BlockDisplay.class);
        minecraftClassMap.put(EntityType.ITEM_DISPLAY, Display.ItemDisplay.class);
        minecraftClassMap.put(EntityType.TEXT_DISPLAY, Display.TextDisplay.class);
        minecraftClassMap.put(EntityType.INTERACTION, net.minecraft.world.entity.Interaction.class);
        minecraftClassMap.put(EntityType.SILVERFISH, Silverfish.class);
        minecraftClassMap.put(EntityType.SKELETON, Skeleton.class);
        minecraftClassMap.put(EntityType.SKELETON_HORSE, SkeletonHorse.class);
        minecraftClassMap.put(EntityType.SLIME, Slime.class);
        minecraftClassMap.put(EntityType.SMALL_FIREBALL, SmallFireball.class);
        minecraftClassMap.put(EntityType.SNOW_GOLEM, SnowGolem.class);
        minecraftClassMap.put(EntityType.SNOWBALL, Snowball.class);
        minecraftClassMap.put(EntityType.SPECTRAL_ARROW, SpectralArrow.class);
        minecraftClassMap.put(EntityType.SPIDER, Spider.class);
        minecraftClassMap.put(EntityType.SQUID, Squid.class);
        minecraftClassMap.put(EntityType.STRAY, Stray.class);
        minecraftClassMap.put(EntityType.STRIDER, Strider.class);
        minecraftClassMap.put(EntityType.TADPOLE, Tadpole.class);
        minecraftClassMap.put(EntityType.EGG, ThrownEgg.class);
        minecraftClassMap.put(EntityType.ENDER_PEARL, ThrownEnderpearl.class);
        minecraftClassMap.put(EntityType.EXPERIENCE_BOTTLE, ThrownExperienceBottle.class);
        minecraftClassMap.put(EntityType.POTION, ThrownPotion.class);
        minecraftClassMap.put(EntityType.TRIDENT, ThrownTrident.class);
        minecraftClassMap.put(EntityType.TRADER_LLAMA, TraderLlama.class);
        minecraftClassMap.put(EntityType.TROPICAL_FISH, TropicalFish.class);
        minecraftClassMap.put(EntityType.TURTLE, Turtle.class);
        minecraftClassMap.put(EntityType.VEX, Vex.class);
        minecraftClassMap.put(EntityType.VILLAGER, Villager.class);
        minecraftClassMap.put(EntityType.VINDICATOR, Vindicator.class);
        minecraftClassMap.put(EntityType.WANDERING_TRADER, WanderingTrader.class);
        minecraftClassMap.put(EntityType.WARDEN, Warden.class);
        minecraftClassMap.put(EntityType.WITCH, Witch.class);
        minecraftClassMap.put(EntityType.WITHER, WitherBoss.class);
        minecraftClassMap.put(EntityType.WITHER_SKELETON, WitherSkeleton.class);
        minecraftClassMap.put(EntityType.WITHER_SKULL, WitherSkull.class);
        minecraftClassMap.put(EntityType.WOLF, Wolf.class);
        minecraftClassMap.put(EntityType.ZOGLIN, Zoglin.class);
        minecraftClassMap.put(EntityType.ZOMBIE, Zombie.class);
        minecraftClassMap.put(EntityType.ZOMBIE_HORSE, ZombieHorse.class);
        minecraftClassMap.put(EntityType.ZOMBIE_VILLAGER, ZombieVillager.class);
        minecraftClassMap.put(EntityType.ZOMBIFIED_PIGLIN, ZombifiedPiglin.class);
        minecraftClassMap.put(EntityType.FISHING_BOBBER, FishingHook.class);
    }
}