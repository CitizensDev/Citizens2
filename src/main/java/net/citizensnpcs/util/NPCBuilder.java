package net.citizensnpcs.util;

import java.util.EnumMap;
import java.util.Map;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.entity.CitizensBlazeNPC;
import net.citizensnpcs.npc.entity.CitizensCaveSpiderNPC;
import net.citizensnpcs.npc.entity.CitizensChickenNPC;
import net.citizensnpcs.npc.entity.CitizensCowNPC;
import net.citizensnpcs.npc.entity.CitizensCreeperNPC;
import net.citizensnpcs.npc.entity.CitizensEnderDragonNPC;
import net.citizensnpcs.npc.entity.CitizensEndermanNPC;
import net.citizensnpcs.npc.entity.CitizensGhastNPC;
import net.citizensnpcs.npc.entity.CitizensGiantNPC;
import net.citizensnpcs.npc.entity.CitizensHumanNPC;
import net.citizensnpcs.npc.entity.CitizensIronGolemNPC;
import net.citizensnpcs.npc.entity.CitizensMagmaCubeNPC;
import net.citizensnpcs.npc.entity.CitizensMushroomCowNPC;
import net.citizensnpcs.npc.entity.CitizensOcelotNPC;
import net.citizensnpcs.npc.entity.CitizensPigNPC;
import net.citizensnpcs.npc.entity.CitizensPigZombieNPC;
import net.citizensnpcs.npc.entity.CitizensSheepNPC;
import net.citizensnpcs.npc.entity.CitizensSilverfishNPC;
import net.citizensnpcs.npc.entity.CitizensSkeletonNPC;
import net.citizensnpcs.npc.entity.CitizensSlimeNPC;
import net.citizensnpcs.npc.entity.CitizensSnowmanNPC;
import net.citizensnpcs.npc.entity.CitizensSpiderNPC;
import net.citizensnpcs.npc.entity.CitizensSquidNPC;
import net.citizensnpcs.npc.entity.CitizensVillagerNPC;
import net.citizensnpcs.npc.entity.CitizensWolfNPC;
import net.citizensnpcs.npc.entity.CitizensZombieNPC;

import org.bukkit.entity.EntityType;

public class NPCBuilder {
    // TODO: convert this into solely a lookup class.
    public CitizensNPC getByType(EntityType type, CitizensNPCManager npcManager, int id, String name) {
        Class<? extends CitizensNPC> npcClass = types.get(type);
        if (npcClass == null)
            return null;
        try {
            return npcClass.getConstructor(CitizensNPCManager.class, int.class, String.class).newInstance(npcManager,
                    id, name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static final Map<EntityType, Class<? extends CitizensNPC>> types = new EnumMap<EntityType, Class<? extends CitizensNPC>>(
            EntityType.class);

    static {
        types.put(EntityType.BLAZE, CitizensBlazeNPC.class);
        types.put(EntityType.CAVE_SPIDER, CitizensCaveSpiderNPC.class);
        types.put(EntityType.CHICKEN, CitizensChickenNPC.class);
        types.put(EntityType.COW, CitizensCowNPC.class);
        types.put(EntityType.CREEPER, CitizensCreeperNPC.class);
        types.put(EntityType.ENDER_DRAGON, CitizensEnderDragonNPC.class);
        types.put(EntityType.ENDERMAN, CitizensEndermanNPC.class);
        types.put(EntityType.GHAST, CitizensGhastNPC.class);
        types.put(EntityType.GIANT, CitizensGiantNPC.class);
        types.put(EntityType.IRON_GOLEM, CitizensIronGolemNPC.class);
        types.put(EntityType.MAGMA_CUBE, CitizensMagmaCubeNPC.class);
        types.put(EntityType.MUSHROOM_COW, CitizensMushroomCowNPC.class);
        types.put(EntityType.OCELOT, CitizensOcelotNPC.class);
        types.put(EntityType.PIG, CitizensPigNPC.class);
        types.put(EntityType.PIG_ZOMBIE, CitizensPigZombieNPC.class);
        types.put(EntityType.PLAYER, CitizensHumanNPC.class);
        types.put(EntityType.SHEEP, CitizensSheepNPC.class);
        types.put(EntityType.SILVERFISH, CitizensSilverfishNPC.class);
        types.put(EntityType.SKELETON, CitizensSkeletonNPC.class);
        types.put(EntityType.SLIME, CitizensSlimeNPC.class);
        types.put(EntityType.SNOWMAN, CitizensSnowmanNPC.class);
        types.put(EntityType.SPIDER, CitizensSpiderNPC.class);
        types.put(EntityType.SQUID, CitizensSquidNPC.class);
        types.put(EntityType.VILLAGER, CitizensVillagerNPC.class);
        types.put(EntityType.WOLF, CitizensWolfNPC.class);
        types.put(EntityType.ZOMBIE, CitizensZombieNPC.class);
    }
}