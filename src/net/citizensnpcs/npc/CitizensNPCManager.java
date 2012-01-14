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

import net.citizensnpcs.api.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.npc.trait.LocationTrait;
import net.citizensnpcs.resources.lib.CraftNPC;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

public class CitizensNPCManager implements NPCManager {
	private Map<LivingEntity, NPC> spawned = new HashMap<LivingEntity, NPC>();

	@Override
	public NPC createNPC() {
		return createNPC(null);
	}

	@Override
	public NPC createNPC(Character character) {
		return createNPC(character);
	}

	@Override
	public NPC createNPC(Character character, Trait... traits) {
		return new CitizensNPC(character, traits);
	}

	@Override
	public NPC getNPC(int id) {
		for (NPC npc : spawned.values()) {
			if (npc.getId() == id) {
				return npc;
			}
		}
		return null;
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
		return getNPCs(Citizens.getTraitManager().getTrait(name));
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

	public CraftNPC spawn(NPC npc) {
		Location loc = ((LocationTrait) npc.getTrait("location")).getLocation();
		WorldServer ws = getWorldServer(loc.getWorld());
		CraftNPC mcEntity = new CraftNPC(getMinecraftServer(ws.getServer()), ws, "", new ItemInWorldManager(ws));
		mcEntity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		ws.addEntity(mcEntity);
		ws.players.remove(mcEntity);

		spawned.put(mcEntity.getPlayer(), npc);
		return mcEntity;
	}

	public void despawn(NPC npc) {
		CraftNPC mcEntity = ((CitizensNPC) npc).getHandle();
		getWorldServer(((LocationTrait) npc.getTrait("location")).getLocation().getWorld()).removeEntity(mcEntity);
		spawned.remove(mcEntity.getPlayer());
	}

	private WorldServer getWorldServer(World world) {
		return ((CraftWorld) world).getHandle();
	}

	private MinecraftServer getMinecraftServer(Server server) {
		return ((CraftServer) server).getServer();
	}
}