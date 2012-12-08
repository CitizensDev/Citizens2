package net.citizensnpcs.npc;

import java.util.Map;

import net.citizensnpcs.npc.entity.BatController;
import net.citizensnpcs.npc.entity.BlazeController;
import net.citizensnpcs.npc.entity.CaveSpiderController;
import net.citizensnpcs.npc.entity.ChickenController;
import net.citizensnpcs.npc.entity.CowController;
import net.citizensnpcs.npc.entity.CreeperController;
import net.citizensnpcs.npc.entity.EnderDragonController;
import net.citizensnpcs.npc.entity.EndermanController;
import net.citizensnpcs.npc.entity.GhastController;
import net.citizensnpcs.npc.entity.GiantController;
import net.citizensnpcs.npc.entity.HumanController;
import net.citizensnpcs.npc.entity.IronGolemController;
import net.citizensnpcs.npc.entity.MagmaCubeController;
import net.citizensnpcs.npc.entity.MushroomCowController;
import net.citizensnpcs.npc.entity.OcelotController;
import net.citizensnpcs.npc.entity.PigController;
import net.citizensnpcs.npc.entity.PigZombieController;
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

import org.bukkit.entity.EntityType;

import com.google.common.collect.Maps;

public class EntityControllers {

    private static final Map<EntityType, Class<? extends EntityController>> TYPES = Maps
            .newEnumMap(EntityType.class);
    public static EntityController createForType(EntityType type) {
        Class<? extends EntityController> controllerClass = TYPES.get(type);
        if (controllerClass == null)
            throw new IllegalArgumentException("Invalid EntityType: " + type);
        try {
            return controllerClass.newInstance();
        } catch (Throwable ex) {
            if (ex.getCause() != null)
                ex = ex.getCause();
            ex.printStackTrace();
            return null;
        }
    }

    static {
        TYPES.put(EntityType.BAT, BatController.class);
        TYPES.put(EntityType.BLAZE, BlazeController.class);
        TYPES.put(EntityType.CAVE_SPIDER, CaveSpiderController.class);
        TYPES.put(EntityType.CHICKEN, ChickenController.class);
        TYPES.put(EntityType.COW, CowController.class);
        TYPES.put(EntityType.CREEPER, CreeperController.class);
        TYPES.put(EntityType.ENDER_DRAGON, EnderDragonController.class);
        TYPES.put(EntityType.ENDERMAN, EndermanController.class);
        TYPES.put(EntityType.GHAST, GhastController.class);
        TYPES.put(EntityType.GIANT, GiantController.class);
        TYPES.put(EntityType.IRON_GOLEM, IronGolemController.class);
        TYPES.put(EntityType.MAGMA_CUBE, MagmaCubeController.class);
        TYPES.put(EntityType.MUSHROOM_COW, MushroomCowController.class);
        TYPES.put(EntityType.OCELOT, OcelotController.class);
        TYPES.put(EntityType.PIG, PigController.class);
        TYPES.put(EntityType.PIG_ZOMBIE, PigZombieController.class);
        TYPES.put(EntityType.PLAYER, HumanController.class);
        TYPES.put(EntityType.SHEEP, SheepController.class);
        TYPES.put(EntityType.SILVERFISH, SilverfishController.class);
        TYPES.put(EntityType.SKELETON, SkeletonController.class);
        TYPES.put(EntityType.SLIME, SlimeController.class);
        TYPES.put(EntityType.SNOWMAN, SnowmanController.class);
        TYPES.put(EntityType.SPIDER, SpiderController.class);
        TYPES.put(EntityType.SQUID, SquidController.class);
        TYPES.put(EntityType.VILLAGER, VillagerController.class);
        TYPES.put(EntityType.WOLF, WolfController.class);
        TYPES.put(EntityType.WITCH, WitchController.class);
        TYPES.put(EntityType.WITHER, WitherController.class);
        TYPES.put(EntityType.ZOMBIE, ZombieController.class);
    }
}
