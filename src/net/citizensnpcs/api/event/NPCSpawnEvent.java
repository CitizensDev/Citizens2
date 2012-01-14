package net.citizensnpcs.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC spawns
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {
	private static final long serialVersionUID = 5459272868175393832L;

	private final Location location;
	private boolean cancelled = false;

	public NPCSpawnEvent(NPC npc, Location location) {
		super("NPCSpawnEvent", npc);
		this.location = location;
	}

	/**
	 * Gets the location where the NPC was spawned
	 * 
	 * @return Location where the NPC was spawned
	 */
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}