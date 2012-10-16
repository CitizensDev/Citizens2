package net.citizensnpcs.npc;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import net.citizensnpcs.NPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.npc.ai.NPCHolder;
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
import net.citizensnpcs.util.ByIdArray;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class CitizensNPCRegistry implements NPCRegistry {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final NPCDataStore saves;
    private final Map<EntityType, Class<? extends CitizensNPC>> types = new EnumMap<EntityType, Class<? extends CitizensNPC>>(
            EntityType.class);

    public CitizensNPCRegistry(NPCDataStore store) {
        saves = store;

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

    public NPC createNPC(EntityType type, int id, String name) {
        CitizensNPC npc = getByType(type, id, name);
        if (npc == null)
            throw new IllegalStateException("Could not create NPC.");
        npcs.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, generateUniqueId(), name);
    }

    @Override
    public void deregister(NPC npc) {
        npcs.remove(npc.getId());
        saves.remove(npc);
        npc.despawn();
    }

    @Override
    public void deregisterAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            npc.despawn();
            for (Trait t : npc.getTraits())
                t.onRemove();
            saves.remove(npc);
        }
    }

    private int generateUniqueId() {
        int count = 0;
        while (getById(count++) != null)
            ; // TODO: doesn't respect existing save data that might not have
              // been loaded. This causes DBs with NPCs that weren't loaded to
              // have conflicting primary keys.
        return count - 1;
    }

    @Override
    public NPC getById(int id) {
        if (id < 0)
            throw new IllegalArgumentException("invalid id");
        return npcs.get(id);
    }

    private CitizensNPC getByType(EntityType type, int id, String name) {
        Class<? extends CitizensNPC> npcClass = types.get(type);
        if (npcClass == null)
            throw new IllegalArgumentException("Invalid EntityType: " + type);
        try {
            return npcClass.getConstructor(int.class, String.class).newInstance(id, name);
        } catch (Throwable ex) {
            if (ex.getCause() != null)
                ex = ex.getCause();
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof NPCHolder)
            return ((NPCHolder) entity).getNPC();
        net.minecraft.server.Entity handle = ((CraftEntity) entity).getHandle();
        return handle instanceof NPCHolder ? ((NPCHolder) handle).getNPC() : null;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }
}