package net.citizensnpcs.npc;

import java.util.Map;

import org.bukkit.entity.EntityType;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import net.citizensnpcs.npc.entity.BatController;
import net.citizensnpcs.npc.entity.BlazeController;
import net.citizensnpcs.npc.entity.CaveSpiderController;
import net.citizensnpcs.npc.entity.ChickenController;
import net.citizensnpcs.npc.entity.CowController;
import net.citizensnpcs.npc.entity.CreeperController;
import net.citizensnpcs.npc.entity.EnderDragonController;
import net.citizensnpcs.npc.entity.EndermanController;
import net.citizensnpcs.npc.entity.EndermiteController;
import net.citizensnpcs.npc.entity.GhastController;
import net.citizensnpcs.npc.entity.GiantController;
import net.citizensnpcs.npc.entity.GuardianController;
import net.citizensnpcs.npc.entity.HorseController;
import net.citizensnpcs.npc.entity.HumanController;
import net.citizensnpcs.npc.entity.IronGolemController;
import net.citizensnpcs.npc.entity.MagmaCubeController;
import net.citizensnpcs.npc.entity.MushroomCowController;
import net.citizensnpcs.npc.entity.OcelotController;
import net.citizensnpcs.npc.entity.PigController;
import net.citizensnpcs.npc.entity.PigZombieController;
import net.citizensnpcs.npc.entity.RabbitController;
import net.citizensnpcs.npc.entity.SheepController;
import net.citizensnpcs.npc.entity.SilverfishController;
import net.citizensnpcs.npc.entity.SkeletonController;
import net.citizensnpcs.npc.entity.SlimeController;
import net.citizensnpcs.npc.entity.SnowmanController;
import net.citizensnpcs.npc.entity.SpiderController;
import net.citizensnpcs.npc.entity.SquidController;
import net.citizensnpcs.npc.entity.VillagerController;
import net.citizensnpcs.npc.entity.WitchController;
import net.citizensnpcs.npc.entity.WitherController;
import net.citizensnpcs.npc.entity.WolfController;
import net.citizensnpcs.npc.entity.ZombieController;
import net.citizensnpcs.npc.entity.nonliving.ArmorStandController;
import net.citizensnpcs.npc.entity.nonliving.BoatController;
import net.citizensnpcs.npc.entity.nonliving.EggController;
import net.citizensnpcs.npc.entity.nonliving.EnderCrystalController;
import net.citizensnpcs.npc.entity.nonliving.EnderPearlController;
import net.citizensnpcs.npc.entity.nonliving.EnderSignalController;
import net.citizensnpcs.npc.entity.nonliving.FallingBlockController;
import net.citizensnpcs.npc.entity.nonliving.FireworkController;
import net.citizensnpcs.npc.entity.nonliving.FishingHookController;
import net.citizensnpcs.npc.entity.nonliving.ItemController;
import net.citizensnpcs.npc.entity.nonliving.ItemFrameController;
import net.citizensnpcs.npc.entity.nonliving.LargeFireballController;
import net.citizensnpcs.npc.entity.nonliving.LeashController;
import net.citizensnpcs.npc.entity.nonliving.MinecartChestController;
import net.citizensnpcs.npc.entity.nonliving.MinecartCommandController;
import net.citizensnpcs.npc.entity.nonliving.MinecartFurnaceController;
import net.citizensnpcs.npc.entity.nonliving.MinecartHopperController;
import net.citizensnpcs.npc.entity.nonliving.MinecartRideableController;
import net.citizensnpcs.npc.entity.nonliving.MinecartTNTController;
import net.citizensnpcs.npc.entity.nonliving.PaintingController;
import net.citizensnpcs.npc.entity.nonliving.SmallFireballController;
import net.citizensnpcs.npc.entity.nonliving.SnowballController;
import net.citizensnpcs.npc.entity.nonliving.TNTPrimedController;
import net.citizensnpcs.npc.entity.nonliving.ThrownExpBottleController;
import net.citizensnpcs.npc.entity.nonliving.ThrownPotionController;
import net.citizensnpcs.npc.entity.nonliving.TippedArrowController;
import net.citizensnpcs.npc.entity.nonliving.WitherSkullController;

public class EntityControllers {
    public static boolean controllerExistsForType(EntityType type) {
        return TYPES.containsKey(type);
    }

    public static EntityController createForType(EntityType type) {
        Class<? extends EntityController> controllerClass = TYPES.get(type);
        if (controllerClass == null)
            throw new IllegalArgumentException("Unknown EntityType: " + type);
        try {
            return controllerClass.newInstance();
        } catch (Throwable ex) {
            Throwables.getRootCause(ex).printStackTrace();
            return null;
        }
    }

    public static void setEntityControllerForType(EntityType type, Class<? extends EntityController> controller) {
        TYPES.put(type, controller);
    }

    private static final Map<EntityType, Class<? extends EntityController>> TYPES = Maps.newEnumMap(EntityType.class);

    static {
        TYPES.put(EntityType.ARROW, TippedArrowController.class);
        TYPES.put(EntityType.ARMOR_STAND, ArmorStandController.class);
        TYPES.put(EntityType.BAT, BatController.class);
        TYPES.put(EntityType.BLAZE, BlazeController.class);
        TYPES.put(EntityType.BOAT, BoatController.class);
        TYPES.put(EntityType.CAVE_SPIDER, CaveSpiderController.class);
        TYPES.put(EntityType.CHICKEN, ChickenController.class);
        TYPES.put(EntityType.COW, CowController.class);
        TYPES.put(EntityType.CREEPER, CreeperController.class);
        TYPES.put(EntityType.DROPPED_ITEM, ItemController.class);
        TYPES.put(EntityType.EGG, EggController.class);
        TYPES.put(EntityType.ENDER_CRYSTAL, EnderCrystalController.class);
        TYPES.put(EntityType.ENDER_DRAGON, EnderDragonController.class);
        TYPES.put(EntityType.ENDER_PEARL, EnderPearlController.class);
        TYPES.put(EntityType.ENDER_SIGNAL, EnderSignalController.class);
        TYPES.put(EntityType.ENDERMAN, EndermanController.class);
        TYPES.put(EntityType.ENDERMITE, EndermiteController.class);
        TYPES.put(EntityType.FALLING_BLOCK, FallingBlockController.class);
        TYPES.put(EntityType.FIREWORK, FireworkController.class);
        TYPES.put(EntityType.FIREBALL, LargeFireballController.class);
        TYPES.put(EntityType.FISHING_HOOK, FishingHookController.class);
        TYPES.put(EntityType.GHAST, GhastController.class);
        TYPES.put(EntityType.GIANT, GiantController.class);
        TYPES.put(EntityType.GUARDIAN, GuardianController.class);
        TYPES.put(EntityType.HORSE, HorseController.class);
        TYPES.put(EntityType.IRON_GOLEM, IronGolemController.class);
        TYPES.put(EntityType.ITEM_FRAME, ItemFrameController.class);
        TYPES.put(EntityType.LEASH_HITCH, LeashController.class);
        TYPES.put(EntityType.MAGMA_CUBE, MagmaCubeController.class);
        TYPES.put(EntityType.MINECART, MinecartRideableController.class);
        TYPES.put(EntityType.MINECART_CHEST, MinecartChestController.class);
        TYPES.put(EntityType.MINECART_COMMAND, MinecartCommandController.class);
        TYPES.put(EntityType.MINECART_FURNACE, MinecartFurnaceController.class);
        TYPES.put(EntityType.MINECART_HOPPER, MinecartHopperController.class);
        TYPES.put(EntityType.MINECART_TNT, MinecartTNTController.class);
        TYPES.put(EntityType.MUSHROOM_COW, MushroomCowController.class);
        TYPES.put(EntityType.OCELOT, OcelotController.class);
        TYPES.put(EntityType.PAINTING, PaintingController.class);
        TYPES.put(EntityType.PIG, PigController.class);
        TYPES.put(EntityType.PIG_ZOMBIE, PigZombieController.class);
        TYPES.put(EntityType.PLAYER, HumanController.class);
        TYPES.put(EntityType.RABBIT, RabbitController.class);
        TYPES.put(EntityType.SHEEP, SheepController.class);
        TYPES.put(EntityType.SILVERFISH, SilverfishController.class);
        TYPES.put(EntityType.SKELETON, SkeletonController.class);
        TYPES.put(EntityType.SLIME, SlimeController.class);
        TYPES.put(EntityType.SMALL_FIREBALL, SmallFireballController.class);
        TYPES.put(EntityType.SNOWBALL, SnowballController.class);
        TYPES.put(EntityType.SNOWMAN, SnowmanController.class);
        TYPES.put(EntityType.SPIDER, SpiderController.class);
        TYPES.put(EntityType.SPLASH_POTION, ThrownPotionController.class);
        TYPES.put(EntityType.SQUID, SquidController.class);
        TYPES.put(EntityType.THROWN_EXP_BOTTLE, ThrownExpBottleController.class);
        TYPES.put(EntityType.PRIMED_TNT, TNTPrimedController.class);
        TYPES.put(EntityType.VILLAGER, VillagerController.class);
        TYPES.put(EntityType.WOLF, WolfController.class);
        TYPES.put(EntityType.WITCH, WitchController.class);
        TYPES.put(EntityType.WITHER, WitherController.class);
        TYPES.put(EntityType.WITHER_SKULL, WitherSkullController.class);
        TYPES.put(EntityType.ZOMBIE, ZombieController.class);
    }
}
