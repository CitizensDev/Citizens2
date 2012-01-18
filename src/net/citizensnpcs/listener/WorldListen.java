package net.citizensnpcs.listener;

import java.util.HashSet;
import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListen implements Listener {
	private Set<Integer> toRespawn = new HashSet<Integer>();

	@EventHandler(event = ChunkLoadEvent.class, priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (int id : toRespawn) {
			NPC npc = CitizensAPI.getNPCManager().getNPC(id);
			npc.spawn(((LocationTrait) npc.getTrait("location")).getLocation());
			toRespawn.remove(id);
		}
	}

	@EventHandler(event = ChunkLoadEvent.class, priority = EventPriority.NORMAL)
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