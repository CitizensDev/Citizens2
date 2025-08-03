package net.citizensnpcs.util;

/**
 * A named head yaw/pitch
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

    public String getName() {
        return name;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public String stringValue() {
        return name + ';' + pitch + ';' + yaw;
    }

    @Override
    public String toString() {
        return "Pose{Name='" + name + "';Pitch='" + pitch + "';Yaw='" + yaw + "';}";
    }
}