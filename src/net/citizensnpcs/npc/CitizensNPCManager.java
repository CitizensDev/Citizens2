package net.citizensnpcs.npc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.resources.lib.CraftNPC;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.WorldServer;

public class CitizensNPCManager implements NPCManager {
    private final Map<Entity, NPC> spawned = new ConcurrentHashMap<Entity, NPC>();
    private final Map<Integer, NPC> byID = new ConcurrentHashMap<Integer, NPC>();

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
        return spawned.get(entity);
    }

    @Override
    public Collection<NPC> getNPCs() {
        return byID.values();
    }

    @Override
    public Collection<NPC> getNPCs(Class<? extends Trait> trait) {
        Set<NPC> npcs = new HashSet<NPC>();
        for (NPC npc : byID.values()) {
            if (npc.hasTrait(trait))
                npcs.add(npc);
        }
        return npcs;
    }

    @Override
    public boolean isNPC(Entity entity) {
        return spawned.containsKey(entity);
    }

    public int getUniqueID() {
        int count = 0;
        while (true) {
            if (getNPC(count) == null)
                break;
            count++;
        }
        return count;
    }

    public CraftNPC spawn(NPC npc, Location loc) {
        WorldServer ws = getWorldServer(loc.getWorld());
        CraftNPC mcEntity = new CraftNPC(getMinecraftServer(ws.getServer()), ws, npc.getFullName(),
                new ItemInWorldManager(ws));
        mcEntity.removeFromPlayerMap(npc.getFullName());
        mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ws.addEntity(mcEntity);
        ws.players.remove(mcEntity);

        spawned.put(mcEntity.getPlayer(), npc);
        return mcEntity;
    }

    public void despawn(NPC npc) {
        CraftNPC mcEntity = ((CitizensNPC) npc).getHandle();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(mcEntity.id));
        }
        Location loc = npc.getBukkitEntity().getLocation();
        getWorldServer(loc.getWorld()).removeEntity(mcEntity);
        npc.getTrait(SpawnLocation.class).setLocation(loc);

        spawned.remove(mcEntity.getPlayer());
    }

    public void remove(NPC npc) {
        if (spawned.containsKey(((CitizensNPC) npc).getHandle()))
            despawn(npc);
        byID.remove(npc.getId());
    }

    private WorldServer getWorldServer(World world) {
        return ((CraftWorld) world).getHandle();
    }

    private MinecraftServer getMinecraftServer(Server server) {
        return ((CraftServer) server).getServer();
    }
}