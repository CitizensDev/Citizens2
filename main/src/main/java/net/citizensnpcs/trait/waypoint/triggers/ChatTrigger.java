package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.Messaging;

public class ChatTrigger implements WaypointTrigger {
    @Persist(required = true)
    private List<String> lines;
    @Persist
    private double radius = -1;

    public ChatTrigger() {
    }

    public ChatTrigger(double radius, Collection<String> chatLines) {
        this.radius = radius;
        lines = Lists.newArrayList(chatLines);
    }

    @Override
    public String description() {
        return String.format("[[Chat]] [radius %f, %s]", radius, Joiner.on(", ").join(lines));
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (radius <= 0) {
            for (Player player : npc.getEntity().getWorld().getPlayers()) {
                for (String line : lines) {
                    Messaging.send(player, line);
                }
            }
        } else {
            for (Player player : CitizensAPI.getLocationLookup().getNearbyVisiblePlayers(npc.getEntity(), radius)) {
                for (String line : lines) {
                    Messaging.send(player, line);
                }
            }
        }
    }
}
