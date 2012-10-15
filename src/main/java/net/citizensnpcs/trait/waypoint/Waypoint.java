package net.citizensnpcs.trait.waypoint;

import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;

import org.bukkit.Location;

public class Waypoint {
    @Persist(required = true)
    private Location location;
    @Persist
    private List<WaypointTrigger> triggers;

    public Waypoint() {
    }

    public Waypoint(Location at) {
        location = at;
    }

    public Location getLocation() {
        return location;
    }

    public void onReach(NPC npc) {
        if (triggers == null)
            return;
        for (WaypointTrigger trigger : triggers)
            trigger.onWaypointReached(npc, location);
    }

    static {
        PersistenceLoader.registerPersistDelegate(WaypointTrigger.class, WaypointTriggerRegistry.class);
    }
}