package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.Location;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;

public class SpeedTrigger implements WaypointTrigger {
    @Persist
    private float speed = 1F;

    public SpeedTrigger() {
    }

    public SpeedTrigger(float speed) {
        this.speed = speed;
    }

    @Override
    public String description() {
        return String.format("[[Speed]] change to %f", speed);
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        npc.getNavigator().getDefaultParameters().speedModifier(speed);
    }
}
