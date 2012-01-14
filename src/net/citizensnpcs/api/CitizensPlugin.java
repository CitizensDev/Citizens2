package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;

/**
 * Contains methods used in order to access the Citizens API
 */
public interface CitizensPlugin {

	/**
	 * Gets the NPCManager
	 * 
	 * @return NPCManager
	 */
	public NPCManager getNPCManager();
}