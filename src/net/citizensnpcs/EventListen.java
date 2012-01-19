package net.citizensnpcs;

import java.util.HashSet;
import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EventListen implements Listener {
	private Set<Integer> toRespawn = new HashSet<Integer>();

	public EventListen(Citizens plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/*
	 * Entity events
	 */
	@EventHandler()
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled() || !CitizensAPI.getNPCManager().isNPC(event.getEntity()))
			return;

		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (e.getDamager() instanceof Player) {
				NPC npc = CitizensAPI.getNPCManager().getNPC(event.getEntity());
				npc.getCharacter().onLeftClick(npc, (Player) e.getDamager());
			}
		}
	}

	@EventHandler()
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled() || !CitizensAPI.getNPCManager().isNPC(event.getEntity())
				|| !(event.getTarget() instanceof Player))
			return;

		NPC npc = CitizensAPI.getNPCManager().getNPC(event.getEntity());
		npc.getCharacter().onRightClick(npc, (Player) event.getTarget());
	}

	/*
	 * World events
	 */
	@EventHandler()
	public void onChunkLoad(ChunkLoadEvent event) {
		for (int id : toRespawn) {
			NPC npc = CitizensAPI.getNPCManager().getNPC(id);
			npc.spawn(((LocationTrait) npc.getTrait("location")).getLocation());
			toRespawn.remove(id);
		}
	}

	@EventHandler()
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled())
			return;

		for (NPC npc : CitizensAPI.getNPCManager().getNPCs()) {
			LocationTrait loc = (LocationTrait) npc.getTrait("location");
			if (event.getWorld().equals(loc.getLocation().getWorld())
					&& event.getChunk().getX() == loc.getLocation().getChunk().getX()
					&& event.getChunk().getZ() == loc.getLocation().getChunk().getZ()) {
				toRespawn.add(npc.getId());
				npc.despawn();
			}
		}
	}
}