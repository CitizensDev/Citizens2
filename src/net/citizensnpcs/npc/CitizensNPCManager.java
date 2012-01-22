package net.citizensnpcs.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private final ByIdArray<NPC> spawned = ByIdArray.create();
    private final ByIdArray<NPC> byID = ByIdArray.create();

    @Override
    public NPC createNPC(String name) {
        return createNPC(name, null);
    }

    @Override
    public NPC createNPC(String name, Character character) {
        CitizensNPC npc = new CitizensNPC(name, character);
        byID.put(npc.getId(), npc);
        return npc;
    }

    @Override
    public NPC getNPC(int id) {
        return byID.get(id);
    }

    @Override
    public NPC getNPC(Entity entity) {
        return spawned.get(entity.getEntityId());
    }

    @Override
    public Iterable<NPC> getAllNPCs() {
        return byID;
    }

    @Override
    public Iterable<NPC> getSpawnedNPCs() {
        return spawned;
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Trait> trait) {
        List<NPC> npcs = new ArrayList<NPC>();
        for (NPC npc : byID) {
            if (npc.hasTrait(trait))
                npcs.add(npc);
        }
        return npcs;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return spawned.contains(entity.getEntityId());
    }

    public int generateUniqueId() {
        int count = 0;
        while (true) {
            if (getNPC(count) == null)
                break;
            count++;
        }
        return count;
    }

    public CraftNPC spawn(NPC npc, Location loc) {
        if (spawned.contains(npc.getBukkitEntity().getEntityId()))
            throw new IllegalStateException("The NPC with ID '" + npc.getId() + "' is already spawned.");
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

    public void despawn(NPC npc) {
        if (!spawned.contains(npc.getBukkitEntity().getEntityId()))
            throw new IllegalStateException("The NPC with ID '" + npc.getId() + "' is already despawned.");
        CraftNPC mcEntity = ((CitizensNPC) npc).getHandle();
        for (Player player : Bukkit.getOnlinePlayers())
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(mcEntity.id));
        Location loc = npc.getBukkitEntity().getLocation();
        getWorldServer(loc.getWorld()).removeEntity(mcEntity);
        npc.getTrait(SpawnLocation.class).setLocation(loc);

        spawned.remove(mcEntity.getPlayer().getEntityId());
    }

    public void remove(NPC npc) {
        if (spawned.contains(npc.getBukkitEntity().getEntityId()))
            despawn(npc);
        byID.remove(npc.getId());
    }

    private WorldServer getWorldServer(World world) {
        return ((CraftWorld) world).getHandle();
    }

    private MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }

    public int size() {
        return byID.size();
    }
}