package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.abstraction.WorldVector;

public class Waypoint {
    private final WorldVector location;

    public Waypoint(WorldVector location) {
        this.location = location;
    }

    public WorldVector getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Waypoint other = (Waypoint) obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }
}