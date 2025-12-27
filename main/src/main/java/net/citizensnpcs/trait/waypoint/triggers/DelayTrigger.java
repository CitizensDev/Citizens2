package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.Location;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class DelayTrigger implements WaypointTrigger {
    @Persist
    private int delay = 0;

    public DelayTrigger() {
    }

    public DelayTrigger(int delay) {
        this.delay = delay;
    }

    private void delay(WaypointProvider provider) {
        provider.setPaused(true);
        CitizensAPI.getScheduler().runTaskLater(() -> provider.setPaused(false), delay);
    }

    @Override
    public String description() {
        return String.format("[[Delay]] for [[%d]] ticks", delay);
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (delay > 0) {
            delay(npc.getOrAddTrait(Waypoints.class).getCurrentProvider());
        }
    }
}
