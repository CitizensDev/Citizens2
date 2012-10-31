package net.citizensnpcs.trait.waypoint;

import java.util.Collections;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.trait.waypoint.triggers.DelayTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTriggerRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.common.collect.Lists;

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

    public void addTrigger(WaypointTrigger trigger) {
        if (triggers == null)
            triggers = Lists.newArrayList();
        triggers.add(trigger);
    }

    public Location getLocation() {
        return location;
    }

    @SuppressWarnings("unchecked")
    public List<WaypointTrigger> getTriggers() {
        return triggers == null ? Collections.EMPTY_LIST : triggers;
    }

    public void onReach(NPC npc) {
        if (triggers == null)
            return;
        runTriggers(npc, 0);
    }

    private void runTriggers(final NPC npc, int start) {
        for (int i = start; i < triggers.size(); i++) {
            WaypointTrigger trigger = triggers.get(i);
            trigger.onWaypointReached(npc, location);
            if (trigger instanceof DelayTrigger) {
                int delay = ((DelayTrigger) trigger).getDelay();
                if (delay <= 0)
                    continue;
                final int newStart = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        runTriggers(npc, newStart);
                    }
                }, delay);
                break;
            }
        }
    }

    static {
        PersistenceLoader.registerPersistDelegate(WaypointTrigger.class, WaypointTriggerRegistry.class);
    }
}