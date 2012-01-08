package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.character.CharacterManager;

public class Citizens {
	private static final Citizens instance = new Citizens();

	private NPCManager npcManager = null;
	private CharacterManager characterManager = null;

	private Citizens() {
	}

	public static Citizens getInstance() {
		return instance;
	}

	public static NPCManager getNPCManager() {
		return getInstance().npcManager;
	}

	public void setNPCManager(NPCManager npcManager) {
		if (this.npcManager == null) {
			this.npcManager = npcManager;
		}
	}

	public static CharacterManager getNPCTypeManager() {
		return getInstance().characterManager;
	}

	public void setNPCTypeManager(CharacterManager characterManager) {
		if (this.characterManager == null) {
			this.characterManager = characterManager;
		}
	}
}