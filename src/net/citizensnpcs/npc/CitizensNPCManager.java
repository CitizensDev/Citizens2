package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.resource.lib.CraftNPC;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class CitizensNPCManager implements NPCManager {
    private final ByIdArray<NPC> npcs = new ByIdArray<NPC>();
    private final SetMultimap<Integer, String> selected = HashMultimap.create();
    private final Storage saves;

    public CitizensNPCManager(Storage saves) {
        this.saves = saves;
    }

    @Override
    public NPC createNPC(String name) {
        return createNPC(name, null);
    }

    @Override
    public NPC createNPC(String name, Character character) {
        return createNPC(generateUniqueId(), name, character);
    }

    public NPC createNPC(int id, String name, Character character) {
        if (npcs.contains(id))
            throw new IllegalArgumentException("An NPC already has the ID '" + id + "'.");

        CitizensNPC npc = new CitizensNPC(this, id, name);
        npc.setCharacter(character);
        npcs.put(npc.getId(), npc);
        return npc;
    }

    public void despawn(NPC npc) {
        npc.getTrait(SpawnLocation.class).setLocation(npc.getBukkitEntity().getLocation());
        selected.removeAll(npc.getId());
        npc.getBukkitEntity().remove();
    }

    @Override
    public Iterator<NPC> iterator() {
        return npcs.iterator();
    }

    private MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }

    @Override
    public NPC getNPC(Entity entity) {
        Messaging.log("Version: " + getMinecraftServer(Bukkit.getServer()).getVersion());
        for (NPC npc : npcs)
            if (npc.isSpawned() && npc.getBukkitEntity().getEntityId() == entity.getEntityId())
                return npc;
        return null;
    }

    @Override
    public NPC getNPC(int id) {
        return npcs.get(id);
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Character> character) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : this)
            if (npc.getCharacter() != null && npc.getCharacter().getClass().equals(character))
                npcs.add(npc);
        return npcs;
    }

    private int generateUniqueId() {
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
        return getNPC(entity) != null;
    }

    public void remove(NPC npc) {
        if (npc.isSpawned())
            despawn(npc);
        npcs.remove(npc.getId());
        saves.getKey("npc").removeKey("" + npc.getId());
        selected.removeAll(npc.getId());
    }

    public CraftNPC spawn(NPC npc, Location loc) {
        WorldServer ws = getWorldServer(loc.getWorld());
        CraftNPC mcEntity = new CraftNPC(getMinecraftServer(ws.getServer()), ws, npc.getFullName(),
                new ItemInWorldManager(ws));
        mcEntity.removeFromPlayerMap(npc.getFullName());
        mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ws.addEntity(mcEntity);
        ws.players.remove(mcEntity);
        return mcEntity;
    }

    public void selectNPC(Player player, NPC npc) {
        // Remove existing selection if any
        NPC existing = getSelectedNPC(player);
        if (existing != null)
            selected.get(existing.getId()).remove(player.getName());
        selected.put(npc.getId(), player.getName());
    }

    public boolean npcIsSelectedByPlayer(Player player, NPC npc) {
        if (!selected.containsKey(npc.getId()))
            return false;
        return selected.get(npc.getId()).contains(player.getName());
    }

    public NPC getSelectedNPC(Player player) {
        for (int id : selected.keySet()) {
            if (selected.get(id).contains(player.getName()))
                return getNPC(id);
        }
        return null;
    }
}