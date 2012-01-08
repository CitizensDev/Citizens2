package net.citizensnpcs.api.event;

import org.bukkit.Location;

import net.citizensnpcs.api.npc.NPC;

public class NPCSpawnEvent extends NPCEvent {
	private static final long serialVersionUID = 5459272868175393832L;

	private final Location location;

	public NPCSpawnEvent(NPC<?> npc, Location location) {
		super("NPCSpawnEvent", npc);
		this.location = location;
	}

	/**
	 * Gets the location where the NPC was created
	 * 
	 * @return Location where NPC was created
	 */
	public Location getLocation() {
		return location;
	}
}