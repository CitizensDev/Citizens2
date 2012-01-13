package net.citizensnpcs.api.npc.pathfinding;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface Navigator {
	public void createPath(Location destination);

	public void registerCallback(NavigatorCallback callback);

	public void target(Entity target, boolean aggressive);
}