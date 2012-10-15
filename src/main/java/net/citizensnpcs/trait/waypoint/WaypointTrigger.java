package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;

public interface WaypointTrigger {
    void onWaypointReached(NPC npc, Location waypoint);
}
