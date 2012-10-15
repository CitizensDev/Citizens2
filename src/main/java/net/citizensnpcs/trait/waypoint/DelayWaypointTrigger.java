package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DelayWaypointTrigger implements WaypointTrigger {
    @Persist
    private int delay = 0;

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (delay > 0)
            scheduleTask(npc.getTrait(Waypoints.class).getCurrentProvider());
    }

    private void scheduleTask(final WaypointProvider provider) {
        provider.setPaused(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                provider.setPaused(false);
            }
        }, delay);
    }

    public void setDelay(int newDelay) {
        delay = newDelay;
    }
}
