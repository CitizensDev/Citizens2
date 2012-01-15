package net.citizensnpcs.npc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;
import net.citizensnpcs.resources.lib.CraftNPC;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

public class CitizensNPCManager implements NPCManager {
	private Map<LivingEntity, NPC> spawned = new HashMap<LivingEntity, NPC>();
	private Map<Integer, NPC> byID = new HashMap<Integer, NPC>();

	@Override
	public NPC createNPC(String name) {
		return createNPC(name, null);
	}

	@Override
	public NPC createNPC(String name, Character character) {
		return createNPC(name, character);
	}

	@Override
	public NPC createNPC(String name, Character character, Trait... traits) {
		CitizensNPC npc = new CitizensNPC(name, character, traits);
		byID.put(npc.getId(), npc);
		return npc;
	}

	@Override
	public NPC getNPC(int id) {
		return byID.get(id);
	}

	@Override
	public NPC getNPC(LivingEntity entity) {
		return spawned.get(entity);
	}

	@Override
	public Collection<NPC> getNPCs() {
		return spawned.values();
	}

	@Override
	public Collection<NPC> getNPCs(Trait trait) {
		Set<NPC> npcs = new HashSet<NPC>();
		for (NPC npc : spawned.values()) {
			if (npc.hasTrait(trait)) {
				npcs.add(npc);
			}
		}
		return npcs;
	}

	@Override
	public Collection<NPC> getNPCs(String name) {
		Set<NPC> npcs = new HashSet<NPC>();
		for (NPC npc : spawned.values()) {
			if (npc.hasTrait(name)) {
				npcs.add(npc);
			}
		}
		return npcs;
	}

	@Override
	public boolean isNPC(LivingEntity entity) {
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
		mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		ws.addEntity(mcEntity);
		ws.players.remove(mcEntity);

		spawned.put(mcEntity.getPlayer(), npc);
		return mcEntity;
	}

	public void despawn(NPC npc) {
		CraftNPC mcEntity = ((CitizensNPC) npc).getHandle();
		// TODO send Packet29DestroyEntity
		getWorldServer(((LocationTrait) npc.getTrait("location")).getLocation().getWorld()).removeEntity(mcEntity);
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