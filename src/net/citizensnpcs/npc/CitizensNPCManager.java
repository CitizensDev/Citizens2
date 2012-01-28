package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.trait.Owner;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.resources.lib.CraftNPC;
import net.citizensnpcs.storage.Storage;
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
    public Iterator<NPC> iterator() {
        return byID.iterator();
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
    public Collection<NPC> getNPCs(Class<? extends Character> character) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : this) {
            if (npc.getCharacter() != null
                    && CitizensAPI.getCharacterManager().getInstance(npc.getCharacter().getName(), npc) != null)
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
        saves.getKey("npc").removeKey("" + npc.getId());
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

    public void deselectNPC(Player player) {
        selected.remove(player.getName());
    }

    public boolean npcIsSelectedByPlayer(Player player, NPC npc) {
        if (!selected.containsKey(player.getName()))
            return false;
        return selected.get(player.getName()) == npc.getId();
    }

    public boolean hasNPCSelected(Player player) {
        return selected.containsKey(player.getName());
    }

    public boolean canSelectNPC(Player player, NPC npc) {
        if (player.hasPermission("citizens.npc.select"))
            return player.getItemInHand().getTypeId() == Setting.SELECTION_ITEM.getInt()
                    && npc.getTrait(Owner.class).getOwner().equals(player.getName());
        return false;
    }

    public NPC getSelectedNPC(Player player) {
        if (!selected.containsKey(player.getName()))
            return null;
        return getNPC(selected.get(player.getName()));
    }

    public int size() {
        return byID.size();
    }
}