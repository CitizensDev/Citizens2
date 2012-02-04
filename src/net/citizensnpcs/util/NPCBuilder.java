package net.citizensnpcs.util;

import java.util.HashMap;
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
import net.citizensnpcs.npc.entity.CitizensMagmaCubeNPC;
import net.citizensnpcs.npc.entity.CitizensMushroomCowNPC;
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

import org.bukkit.entity.CreatureType;

public class NPCBuilder {
    private static final Map<CreatureType, Class<? extends CitizensNPC>> types = new HashMap<CreatureType, Class<? extends CitizensNPC>>();

    static {
        types.put(CreatureType.BLAZE, CitizensBlazeNPC.class);
        types.put(CreatureType.CAVE_SPIDER, CitizensCaveSpiderNPC.class);
        types.put(CreatureType.CHICKEN, CitizensChickenNPC.class);
        types.put(CreatureType.COW, CitizensCowNPC.class);
        types.put(CreatureType.CREEPER, CitizensCreeperNPC.class);
        types.put(CreatureType.ENDER_DRAGON, CitizensEnderDragonNPC.class);
        types.put(CreatureType.ENDERMAN, CitizensEndermanNPC.class);
        types.put(CreatureType.GHAST, CitizensGhastNPC.class);
        types.put(CreatureType.GIANT, CitizensGiantNPC.class);
        types.put(CreatureType.MAGMA_CUBE, CitizensMagmaCubeNPC.class);
        types.put(CreatureType.MONSTER, CitizensHumanNPC.class);
        types.put(CreatureType.MUSHROOM_COW, CitizensMushroomCowNPC.class);
        types.put(CreatureType.PIG, CitizensPigNPC.class);
        types.put(CreatureType.PIG_ZOMBIE, CitizensPigZombieNPC.class);
        types.put(CreatureType.SHEEP, CitizensSheepNPC.class);
        types.put(CreatureType.SILVERFISH, CitizensSilverfishNPC.class);
        types.put(CreatureType.SKELETON, CitizensSkeletonNPC.class);
        types.put(CreatureType.SLIME, CitizensSlimeNPC.class);
        types.put(CreatureType.SNOWMAN, CitizensSnowmanNPC.class);
        types.put(CreatureType.SPIDER, CitizensSpiderNPC.class);
        types.put(CreatureType.SQUID, CitizensSquidNPC.class);
        types.put(CreatureType.VILLAGER, CitizensVillagerNPC.class);
        types.put(CreatureType.WOLF, CitizensWolfNPC.class);
        types.put(CreatureType.ZOMBIE, CitizensZombieNPC.class);
    }

    public CitizensNPC getByType(CreatureType type, CitizensNPCManager npcManager, int id, String name) {
        Class<? extends CitizensNPC> npcClass = types.get(type);
        try {
            return npcClass.getConstructor(CitizensNPCManager.class, int.class, String.class).newInstance(npcManager,
                    id, name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}