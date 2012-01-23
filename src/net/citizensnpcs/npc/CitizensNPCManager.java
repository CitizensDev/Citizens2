package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.resources.lib.CraftNPC;
import net.citizensnpcs.util.ByIdArray;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CitizensNPCManager implements NPCManager {
    private final ByIdArray<NPC> spawned = new ByIdArray<NPC>();
    private final ByIdArray<NPC> byID = new ByIdArray<NPC>();
    private final Map<String, Integer> selected = new ConcurrentHashMap<String, Integer>();

    @Override
    public NPC createNPC(String name) {
        return createNPC(name, null);
    }

    @Override
    public NPC createNPC(String name, Character character) {
        return createNPC(generateUniqueId(), name, character);
    }

    public NPC createNPC(int id, String name, Character character) {
        CitizensNPC npc = new CitizensNPC(this, id, name);
        npc.setCharacter(character);
        byID.put(npc.getId(), npc);
        return npc;
    }

    public void despawn(NPC npc) {
        CraftNPC mcEntity = ((CitizensNPC) npc).getHandle();
        for (Player player : Bukkit.getOnlinePlayers())
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(mcEntity.id));
        Location loc = npc.getBukkitEntity().getLocation();
        getWorldServer(loc.getWorld()).removeEntity(mcEntity);
        npc.getTrait(SpawnLocation.class).setLocation(loc);

        spawned.remove(mcEntity.getPlayer().getEntityId());
    }

    @Override
    public Iterable<NPC> getAllNPCs() {
        return byID;
    }

    private MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }

    @Override
    public NPC getNPC(Entity entity) {
        return spawned.get(entity.getEntityId());
    }

    @Override
    public NPC getNPC(int id) {
        return byID.get(id);
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Trait> trait) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : getAllNPCs()) {
            if (npc.hasTrait(trait))
                npcs.add(npc);
        }
        return npcs;
    }

    @Override
    public Iterable<NPC> getSpawnedNPCs() {
        return spawned;
    }

    public int generateUniqueId() {
        int count = 0;
        while (getNPC(count++) != null)
            ;
        return count - 1;
    }

    private WorldServer getWorldServer(World world) {
        return ((CraftWorld) world).getHandle();
    }

    @Override
    public boolean isNPC(Entity entity) {
        return spawned.contains(entity.getEntityId());
    }

    public void remove(NPC npc) {
        if (spawned.contains(npc.getBukkitEntity().getEntityId()))
            despawn(npc);
        byID.remove(npc.getId());
    }

    public CraftNPC spawn(NPC npc, Location loc) {
        WorldServer ws = getWorldServer(loc.getWorld());
        CraftNPC mcEntity = new CraftNPC(getMinecraftServer(ws.getServer()), ws, npc.getFullName(),
                new ItemInWorldManager(ws));
        mcEntity.removeFromPlayerMap(npc.getFullName());
        mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ws.addEntity(mcEntity);
        ws.players.remove(mcEntity);

        spawned.put(mcEntity.getPlayer().getEntityId(), npc);
        return mcEntity;
    }

    public void selectNPC(Player player, NPC npc) {
        selected.put(player.getName(), npc.getId());
    }

    public boolean canSelect(Player player, NPC npc) {
        if (player.hasPermission("citizens.npc.select")) {
            if (!selected.containsKey(player.getName()))
                return true;
            return selected.get(player.getName()) != npc.getId()
                    && player.getItemInHand().getTypeId() == Setting.SELECTION_ITEM.getInt();
        }
        return false;
    }
}