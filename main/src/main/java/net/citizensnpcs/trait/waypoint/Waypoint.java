package net.citizensnpcs.trait.waypoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.triggers.DelayTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTriggerRegistry;
import net.citizensnpcs.util.Messages;

/**
 * Represents a {@link Location} with a number of {@link WaypointTrigger}s that activate on reaching the location.
 */
public class Waypoint {
    @Persist(required = true)
    private Location location;
    @Persist
    private List<WaypointTrigger> triggers;

    /**
     * For persistence - avoid using otherwise.
     */
    public Waypoint() {
    }

    public Waypoint(Location at) {
        location = at.clone();
    }

    public void addTrigger(WaypointTrigger trigger) {
        if (triggers == null) {
            triggers = Lists.newArrayList();
        }
        triggers.add(trigger);
    }

    public void describeTriggers(CommandSender sender) {
        String base = " ";
        if (triggers == null)
            return;
        for (int i = 0; i < triggers.size(); i++) {
            base += "\n    - " + triggers.get(i).description()
                    + " [<hover:show_text:Remove trigger><click:run_command:/npc path remove_trigger " + i
                    + "><u><red>-</click></hover>]";
        }
        Messaging.sendTr(sender, Messages.WAYPOINT_TRIGGER_LIST, base);
    }

    /**
     * Returns the distance in blocks to another waypoint.
     */
    public double distance(Waypoint dest) {
        return location.distance(dest.location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Waypoint other = (Waypoint) obj;
        if (!Objects.equals(location, other.location) || !Objects.equals(triggers, other.triggers))
            return false;
        return true;
    }

    public Location getLocation() {
        return location.clone();
    }

    public List<WaypointTrigger> getTriggers() {
        return triggers == null ? Collections.<WaypointTrigger> emptyList() : triggers;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = prime + (location == null ? 0 : location.hashCode());
        return prime * result + (triggers == null ? 0 : triggers.hashCode());
    }

    /**
     * Runs waypoint triggers for the given NPC.
     */
    public void onReach(NPC npc) {
        if (triggers == null)
            return;
        runTriggers(npc, 0);
    }

    private void runTriggers(NPC npc, int start) {
        List<WaypointTrigger> triggers = Lists.newArrayList(this.triggers);
        for (int i = start; i < triggers.size(); i++) {
            WaypointTrigger trigger = triggers.get(i);
            trigger.onWaypointReached(npc, location.clone());
            if (!(trigger instanceof DelayTrigger)) {
                continue;
            }
            int delay = ((DelayTrigger) trigger).getDelay();
            if (delay <= 0) {
                continue;
            }
            int newStart = i + 1;
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> runTriggers(npc, newStart),
                    delay);
            break;
        }
    }

    @Override
    public String toString() {
        return "Waypoint [" + location + (triggers == null ? "]" : ", " + triggers.size() + " triggers]");
    }

    static {
        PersistenceLoader.registerPersistDelegate(WaypointTrigger.class, WaypointTriggerRegistry.class);
    }
}