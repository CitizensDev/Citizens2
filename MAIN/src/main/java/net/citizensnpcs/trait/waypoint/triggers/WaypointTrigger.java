package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.Location;

import net.citizensnpcs.api.npc.NPC;

public interface WaypointTrigger {
    String description();

    void onWaypointReached(NPC npc, Location waypoint);
}
