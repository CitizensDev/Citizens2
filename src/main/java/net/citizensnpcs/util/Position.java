package net.citizensnpcs.util;


/*
 * Position object which holds yaw/pitch of the head with a name to identify.
 */

public class Position {
	public final String name;
	private final Float yaw;
	private final Float pitch;

	public Position(String name, float pitch, float yaw) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.name = name;
	}

	@Override
	public String toString() {
	 return "Name: " + name + " Pitch: " + pitch.doubleValue() + " Yaw: " + yaw.doubleValue();	
	}

	public String stringValue() {
		return name + ";" + pitch + ";" + yaw; 
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	@Override
	public boolean equals(Object otherPosition) {
		if (otherPosition == null) return false;
		if (otherPosition.toString() == this.name) return true;
		else return false;
	}

}