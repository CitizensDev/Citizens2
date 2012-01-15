package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;

/**
 * Contains methods used in order to access the Citizens API
 */
public class CitizensAPI {
	private static final CitizensAPI instance = new CitizensAPI();

	private NPCManager npcManager;

	public static NPCManager getNPCManager() {
		return instance.npcManager;
	}

	public static void setNPCManager(NPCManager npcManager) {
		if (instance.npcManager == null) {
			instance.npcManager = npcManager;
		}
	}
}