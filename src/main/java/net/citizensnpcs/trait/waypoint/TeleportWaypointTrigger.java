package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;

import org.bukkit.Location;

public class TeleportWaypointTrigger implements WaypointTrigger {
    @Persist
    private Location location;

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (location != null)
            npc.getBukkitEntity().teleport(waypoint);
    }
}
