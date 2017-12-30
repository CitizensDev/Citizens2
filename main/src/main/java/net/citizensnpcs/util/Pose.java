package net.citizensnpcs.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * Pose object which holds yaw/pitch of the head with a name to identify.
 */

public class Pose {
    private final String name;
    private final float pitch;
    private final float yaw;

    public Pose(String name, float pitch, float yaw) {
        this.yaw = yaw;
        this.pitch = pitch;
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

        Pose op = (Pose) object;
        return new EqualsBuilder().append(name, op.getName()).isEquals();
    }

    public String getName() {
        return name;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 21).append(name).toHashCode();
    }

    public String stringValue() {
        return name + ';' + pitch + ';' + yaw;
    }

    @Override
    public String toString() {
        return "Pose{Name='" + name + "';Pitch='" + pitch + "';Yaw='" + yaw + "';}";
    }

}