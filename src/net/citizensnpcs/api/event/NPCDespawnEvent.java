package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC despawns
 */
public class NPCDespawnEvent extends NPCEvent {
	public NPCDespawnEvent(NPC<?> npc) {
		super("NPCDespawnEvent", npc);
	}

	private static final long serialVersionUID = -6104193791185001957L;
}