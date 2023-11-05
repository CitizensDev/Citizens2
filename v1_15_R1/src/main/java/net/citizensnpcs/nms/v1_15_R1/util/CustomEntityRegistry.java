package net.citizensnpcs.nms.v1_15_R1.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.server.v1_15_R1.*;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends RegistryBlocks implements Supplier<RegistryBlocks<EntityTypes<?>>> {
    private final BiMap<MinecraftKey, EntityTypes> entities = HashBiMap.create();
    private final BiMap<EntityTypes, MinecraftKey> entityClasses = this.entities.inverse();
    private final Map<EntityTypes, Integer> entityIds = Maps.newHashMap();
    private final RegistryBlocks<EntityTypes<?>> wrapped;

    public CustomEntityRegistry(RegistryBlocks<EntityTypes<?>> original) {
        super(original.a().getNamespace());
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

    public EntityTypes findType(Class<?> search) {
        return minecraftClassMap.inverse().get(search);
        /*
        for (Object type : wrapped) {
            if (minecraftClassMap.get(type) == search) {
                return (EntityTypes) type;
            }
        }
        return null;
        */
    }

    @Override
    public Object fromId(int var0) {
        return this.wrapped.fromId(var0);
    }

    @Override
    public RegistryBlocks<EntityTypes<?>> get() {
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
    public Optional getOptional(MinecraftKey var0) {
        if (entities.containsKey(var0))
            return Optional.of(entities.get(var0));
        return this.wrapped.getOptional(var0);
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
    } // replace regex
      // ([A-Z_]+).*?a\(E(.*?)::new.*?$
      // minecraftClassMap.put(EntityTypes.\1, E\2.class);

    private static final BiMap<EntityTypes, Class<?>> minecraftClassMap = HashBiMap.create();
    static {
        minecraftClassMap.put(EntityTypes.AREA_EFFECT_CLOUD, EntityAreaEffectCloud.class);
        minecraftClassMap.put(EntityTypes.ARMOR_STAND, EntityArmorStand.class);
        minecraftClassMap.put(EntityTypes.ARROW, EntityTippedArrow.class);
        minecraftClassMap.put(EntityTypes.BAT, EntityBat.class);
        minecraftClassMap.put(EntityTypes.BEE, EntityBee.class);
        minecraftClassMap.put(EntityTypes.BLAZE, EntityBlaze.class);
        minecraftClassMap.put(EntityTypes.BOAT, EntityBoat.class);
        minecraftClassMap.put(EntityTypes.CAT, EntityCat.class);
        minecraftClassMap.put(EntityTypes.CAVE_SPIDER, EntityCaveSpider.class);
        minecraftClassMap.put(EntityTypes.CHICKEN, EntityChicken.class);
        minecraftClassMap.put(EntityTypes.COD, EntityCod.class);
        minecraftClassMap.put(EntityTypes.COW, EntityCow.class);
        minecraftClassMap.put(EntityTypes.CREEPER, EntityCreeper.class);
        minecraftClassMap.put(EntityTypes.DONKEY, EntityHorseDonkey.class);
        minecraftClassMap.put(EntityTypes.DOLPHIN, EntityDolphin.class);
        minecraftClassMap.put(EntityTypes.DRAGON_FIREBALL, EntityDragonFireball.class);
        minecraftClassMap.put(EntityTypes.DROWNED, EntityDrowned.class);
        minecraftClassMap.put(EntityTypes.ELDER_GUARDIAN, EntityGuardianElder.class);
        minecraftClassMap.put(EntityTypes.END_CRYSTAL, EntityEnderCrystal.class);
        minecraftClassMap.put(EntityTypes.ENDER_DRAGON, EntityEnderDragon.class);
        minecraftClassMap.put(EntityTypes.ENDERMAN, EntityEnderman.class);
        minecraftClassMap.put(EntityTypes.ENDERMITE, EntityEndermite.class);
        minecraftClassMap.put(EntityTypes.EVOKER_FANGS, EntityEvokerFangs.class);
        minecraftClassMap.put(EntityTypes.EVOKER, EntityEvoker.class);
        minecraftClassMap.put(EntityTypes.EXPERIENCE_ORB, EntityExperienceOrb.class);
        minecraftClassMap.put(EntityTypes.EYE_OF_ENDER, EntityEnderSignal.class);
        minecraftClassMap.put(EntityTypes.FALLING_BLOCK, EntityFallingBlock.class);
        minecraftClassMap.put(EntityTypes.FIREWORK_ROCKET, EntityFireworks.class);
        minecraftClassMap.put(EntityTypes.FOX, EntityFox.class);
        minecraftClassMap.put(EntityTypes.GHAST, EntityGhast.class);
        minecraftClassMap.put(EntityTypes.GIANT, EntityGiantZombie.class);
        minecraftClassMap.put(EntityTypes.GUARDIAN, EntityGuardian.class);
        minecraftClassMap.put(EntityTypes.HORSE, EntityHorse.class);
        minecraftClassMap.put(EntityTypes.HUSK, EntityZombieHusk.class);
        minecraftClassMap.put(EntityTypes.ILLUSIONER, EntityIllagerIllusioner.class);
        minecraftClassMap.put(EntityTypes.ITEM, EntityItem.class);
        minecraftClassMap.put(EntityTypes.ITEM_FRAME, EntityItemFrame.class);
        minecraftClassMap.put(EntityTypes.FIREBALL, EntityLargeFireball.class);
        minecraftClassMap.put(EntityTypes.LEASH_KNOT, EntityLeash.class);
        minecraftClassMap.put(EntityTypes.LLAMA, EntityLlama.class);
        minecraftClassMap.put(EntityTypes.LLAMA_SPIT, EntityLlamaSpit.class);
        minecraftClassMap.put(EntityTypes.MAGMA_CUBE, EntityMagmaCube.class);
        minecraftClassMap.put(EntityTypes.MINECART, EntityMinecartRideable.class);
        minecraftClassMap.put(EntityTypes.CHEST_MINECART, EntityMinecartChest.class);
        minecraftClassMap.put(EntityTypes.COMMAND_BLOCK_MINECART, EntityMinecartCommandBlock.class);
        minecraftClassMap.put(EntityTypes.FURNACE_MINECART, EntityMinecartFurnace.class);
        minecraftClassMap.put(EntityTypes.HOPPER_MINECART, EntityMinecartHopper.class);
        minecraftClassMap.put(EntityTypes.SPAWNER_MINECART, EntityMinecartMobSpawner.class);
        minecraftClassMap.put(EntityTypes.TNT_MINECART, EntityMinecartTNT.class);
        minecraftClassMap.put(EntityTypes.MULE, EntityHorseMule.class);
        minecraftClassMap.put(EntityTypes.MOOSHROOM, EntityMushroomCow.class);
        minecraftClassMap.put(EntityTypes.OCELOT, EntityOcelot.class);
        minecraftClassMap.put(EntityTypes.PAINTING, EntityPainting.class);
        minecraftClassMap.put(EntityTypes.PANDA, EntityPanda.class);
        minecraftClassMap.put(EntityTypes.PARROT, EntityParrot.class);
        minecraftClassMap.put(EntityTypes.PIG, EntityPig.class);
        minecraftClassMap.put(EntityTypes.PUFFERFISH, EntityPufferFish.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_PIGMAN, EntityPigZombie.class);
        minecraftClassMap.put(EntityTypes.POLAR_BEAR, EntityPolarBear.class);
        minecraftClassMap.put(EntityTypes.TNT, EntityTNTPrimed.class);
        minecraftClassMap.put(EntityTypes.RABBIT, EntityRabbit.class);
        minecraftClassMap.put(EntityTypes.SALMON, EntitySalmon.class);
        minecraftClassMap.put(EntityTypes.SHEEP, EntitySheep.class);
        minecraftClassMap.put(EntityTypes.SHULKER, EntityShulker.class);
        minecraftClassMap.put(EntityTypes.SHULKER_BULLET, EntityShulkerBullet.class);
        minecraftClassMap.put(EntityTypes.SILVERFISH, EntitySilverfish.class);
        minecraftClassMap.put(EntityTypes.SKELETON, EntitySkeleton.class);
        minecraftClassMap.put(EntityTypes.SKELETON_HORSE, EntityHorseSkeleton.class);
        minecraftClassMap.put(EntityTypes.SLIME, EntitySlime.class);
        minecraftClassMap.put(EntityTypes.SMALL_FIREBALL, EntitySmallFireball.class);
        minecraftClassMap.put(EntityTypes.SNOW_GOLEM, EntitySnowman.class);
        minecraftClassMap.put(EntityTypes.SNOWBALL, EntitySnowball.class);
        minecraftClassMap.put(EntityTypes.SPECTRAL_ARROW, EntitySpectralArrow.class);
        minecraftClassMap.put(EntityTypes.SPIDER, EntitySpider.class);
        minecraftClassMap.put(EntityTypes.SQUID, EntitySquid.class);
        minecraftClassMap.put(EntityTypes.STRAY, EntitySkeletonStray.class);
        minecraftClassMap.put(EntityTypes.TRADER_LLAMA, EntityLlamaTrader.class);
        minecraftClassMap.put(EntityTypes.TROPICAL_FISH, EntityTropicalFish.class);
        minecraftClassMap.put(EntityTypes.TURTLE, EntityTurtle.class);
        minecraftClassMap.put(EntityTypes.EGG, EntityEgg.class);
        minecraftClassMap.put(EntityTypes.ENDER_PEARL, EntityEnderPearl.class);
        minecraftClassMap.put(EntityTypes.EXPERIENCE_BOTTLE, EntityThrownExpBottle.class);
        minecraftClassMap.put(EntityTypes.POTION, EntityPotion.class);
        minecraftClassMap.put(EntityTypes.TRIDENT, EntityThrownTrident.class);
        minecraftClassMap.put(EntityTypes.VEX, EntityVex.class);
        minecraftClassMap.put(EntityTypes.VILLAGER, EntityVillager.class);
        minecraftClassMap.put(EntityTypes.IRON_GOLEM, EntityIronGolem.class);
        minecraftClassMap.put(EntityTypes.VINDICATOR, EntityVindicator.class);
        minecraftClassMap.put(EntityTypes.PILLAGER, EntityPillager.class);
        minecraftClassMap.put(EntityTypes.WANDERING_TRADER, EntityVillagerTrader.class);
        minecraftClassMap.put(EntityTypes.WITCH, EntityWitch.class);
        minecraftClassMap.put(EntityTypes.WITHER, EntityWither.class);
        minecraftClassMap.put(EntityTypes.WITHER_SKELETON, EntitySkeletonWither.class);
        minecraftClassMap.put(EntityTypes.WITHER_SKULL, EntityWitherSkull.class);
        minecraftClassMap.put(EntityTypes.WOLF, EntityWolf.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE, EntityZombie.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_HORSE, EntityHorseZombie.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_VILLAGER, EntityZombieVillager.class);
        minecraftClassMap.put(EntityTypes.PHANTOM, EntityPhantom.class);
        minecraftClassMap.put(EntityTypes.RAVAGER, EntityRavager.class);
        minecraftClassMap.put(EntityTypes.FISHING_BOBBER, EntityFishingHook.class);
    }
}