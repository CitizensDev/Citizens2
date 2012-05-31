package net.citizensnpcs.trait.waypoint;

import javax.xml.stream.Location;

public class Waypoint {
    private final Location location;

    public Waypoint(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}