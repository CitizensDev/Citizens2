package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.ai.NPCHandle;
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

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class CitizensNPCManager implements NPCManager {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final Citizens plugin;
    private final Storage saves;
    private final Map<EntityType, Class<? extends CitizensNPC>> types = new EnumMap<EntityType, Class<? extends CitizensNPC>>(
            EntityType.class);

    public CitizensNPCManager(Citizens plugin, Storage saves) {
        this.plugin = plugin;
        this.saves = saves;

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

    public NPC createNPC(EntityType type, int id, String name, Character character) {
        CitizensNPC npc = getByType(type, id, name);
        if (npc == null)
            throw new IllegalStateException("Could not create NPC.");
        if (character != null)
            npc.setCharacter(character);
        npcs.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC createNPC(EntityType type, String name) {
        return createNPC(type, name, null);
    }

    @Override
    public NPC createNPC(EntityType type, String name, Character character) {
        return createNPC(type, generateUniqueId(), name, character);
    }

    void despawn(NPC npc, boolean keepSelected) {
        if (!keepSelected)
            npc.removeMetadata("selectors", plugin);
        npc.getBukkitEntity().remove();
    }

    private int generateUniqueId() {
        int count = 0;
        while (getNPC(count++) != null)
            ; // TODO: doesn't respect existing save data that might not have
              // been loaded. This causes DBs with NPCs that weren't loaded to
              // have conflicting primary keys.
        return count - 1;
    }

    @Override
    public NPC getNPC(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return null;
        net.minecraft.server.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle instanceof NPCHandle)
            return ((NPCHandle) handle).getNPC();
        return null;
    }

    @Override
    public NPC getNPC(int id) {
        return npcs.get(id);
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Character> character) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : this) {
            if (npc.getCharacter() != null && npc.getCharacter().getClass().equals(character))
                npcs.add(npc);
        }
        return npcs;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return getNPC(entity) != null;
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }

    void remove(NPC npc) {
        npcs.remove(npc.getId());
        removeData(npc);
    }

    public void removeAll() {
        Iterator<NPC> itr = iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            npc.despawn();
            removeData(npc);
            itr.remove();
        }
    }

    private void removeData(NPC npc) {
        saves.getKey("npc").removeKey(String.valueOf(npc.getId()));
        removeMetadata(npc);
    }

    private void removeMetadata(NPC npc) {
        // Remove metadata from selectors
        if (npc.hasMetadata("selectors")) {
            for (MetadataValue value : npc.getMetadata("selectors"))
                if (Bukkit.getPlayer(value.asString()) != null)
                    Bukkit.getPlayer(value.asString()).removeMetadata("selected", plugin);
            npc.removeMetadata("selectors", plugin);
        }
    }

    public void safeRemove() {
        // Destroy all NPCs everywhere besides storage
        Iterator<NPC> itr = this.iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            removeMetadata(npc);
            npc.despawn();
        }
    }

    public void selectNPC(Player player, NPC npc) {
        // Remove existing selection if any
        if (player.hasMetadata("selected"))
            player.removeMetadata("selected", plugin);

        player.setMetadata("selected", new FixedMetadataValue(plugin, npc.getId()));
        npc.setMetadata("selectors", new FixedMetadataValue(plugin, player.getName()));

        // Remove editor if the player has one
        Editor.leave(player);

        // Call selection event
        player.getServer().getPluginManager().callEvent(new NPCSelectEvent(npc, player));
    }

    private CitizensNPC getByType(EntityType type, int id, String name) {
        Class<? extends CitizensNPC> npcClass = types.get(type);
        if (npcClass == null)
            throw new IllegalArgumentException("Invalid EntityType: " + type);
        try {
            return npcClass.getConstructor(CitizensNPCManager.class, int.class, String.class).newInstance(this, id,
                    name);
        } catch (Exception ex) {
            return null;
        }
    }
}