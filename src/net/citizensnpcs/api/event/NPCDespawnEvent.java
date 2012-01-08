package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

public class NPCDespawnEvent extends NPCEvent {
	private static final long serialVersionUID = -6104193791185001957L;

	public NPCDespawnEvent(NPC<?> npc) {
		super("NPCDespawnEvent", npc);
	}
}