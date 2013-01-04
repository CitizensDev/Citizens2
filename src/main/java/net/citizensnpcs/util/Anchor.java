package net.citizensnpcs.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;

/*
 * Anchor object which holds a Location with a name to identify.
 */

public class Anchor {
    private final Location location;
    private final String name;

    public Anchor(String name, Location location) {
        this.location = location;
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (object == this)
            return true;
        if (object.getClass() != getClass())
            return false;

        Anchor op = (Anchor) object;
        return new EqualsBuilder().append(name, op.getName()).isEquals();
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 21).append(name).toHashCode();
    }

    public String stringValue() {
        return name + ";" + location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";"
                + location.getZ();
    }

    @Override
    public String toString() {
        return "Name: " + name + " World: " + location.getWorld().getName() + " Location: " + location.getBlockX()
                + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}