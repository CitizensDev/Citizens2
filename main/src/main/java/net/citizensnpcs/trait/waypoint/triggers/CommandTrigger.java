package net.citizensnpcs.trait.waypoint.triggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.Util;

public class CommandTrigger implements WaypointTrigger {
    @Persist
    private List<String> commands = new ArrayList<>();

    public CommandTrigger() {
    }

    public CommandTrigger(Collection<String> commands) {
        this.commands = Lists.newArrayList(commands);
    }

    @Override
    public String description() {
        return String.format("[[Command]] running %s", Joiner.on(", ").join(commands));
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        for (String command : commands) {
            Util.runCommand(npc, null, command, false, false);
        }
    }
}
