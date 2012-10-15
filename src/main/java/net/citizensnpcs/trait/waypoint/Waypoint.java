package net.citizensnpcs.trait.waypoint;

import java.util.List;

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

    static {
        PersistenceLoader.registerPersistDelegate(WaypointTrigger.class, WaypointTriggerPersister.class);
    }
}