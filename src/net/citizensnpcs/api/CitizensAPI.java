package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.TraitManager;

/**
 * Contains methods used in order to access the Citizens API
 */
public class CitizensAPI {
	private static final CitizensAPI instance = new CitizensAPI();

	private NPCManager npcManager;
	private TraitManager traitManager;

	private CitizensAPI() {
	}

	public static CitizensAPI getInstance() {
		return instance;
	}

	/**
	 * Gets the NPCManager
	 * 
	 * @return NPCManager
	 */
	public static NPCManager getNPCManager() {
		return getInstance().npcManager;
	}

	public void setNPCManager(NPCManager npcManager) {
		// Prevent other plugins from setting the NPCManager
		if (this.npcManager == null) {
			this.npcManager = npcManager;
		}
	}

	/**
	 * Gets the TraitManager
	 * 
	 * @return TraitManager
	 */
	public static TraitManager getTraitManager() {
		return getInstance().traitManager;
	}

	public void setTraitManager(TraitManager traitManager) {
		// Prevent other plugins from setting the TraitManager
		if (this.traitManager == null) {
			this.traitManager = traitManager;
		}
	}
}