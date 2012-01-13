package net.citizensnpcs.api.event;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class NPCListener extends CustomEventListener {

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof NPCSpawnEvent) {
			onNPCSpawn((NPCSpawnEvent) event);
		} else if (event instanceof NPCDespawnEvent) {
			onNPCDespawn((NPCDespawnEvent) event);
		}
	}

	/**
	 * Called when an NPC despawns
	 */
	public void onNPCDespawn(NPCDespawnEvent event) {
	}

	/**
	 * Called when an NPC spawns
	 */
	public void onNPCSpawn(NPCSpawnEvent event) {
	}
}