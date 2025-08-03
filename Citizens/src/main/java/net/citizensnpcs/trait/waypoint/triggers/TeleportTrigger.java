package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;

public class TeleportTrigger implements WaypointTrigger {
    @Persist(required = true)
    private Location location;

    public TeleportTrigger() {
    }

    public TeleportTrigger(Location location) {
        this.location = location;
    }

    @Override
    public String description() {
        return String.format("[[Teleport]] to [%s, %d, %d, %d]", location.getWorld().getName(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (location != null) {
            npc.teleport(location, TeleportCause.PLUGIN);
        }
    }
}
