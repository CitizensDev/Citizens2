package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.StorageUtils;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Lists;

public class LinearWaypointProvider implements WaypointProvider, Iterable<Waypoint> {
    private final GenericWaypointCallback callback = new GenericWaypointCallback(this);

    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public Editor createEditor(final Player player) {
        return new Editor() {
            @Override
            public void begin() {
                player.sendMessage(ChatColor.AQUA + "Entered the linear waypoint editor!");
                player.sendMessage(ChatColor.GREEN + "Left click to add waypoint, right click to remove.");
            }

            @Override
            public void end() {
                player.sendMessage(ChatColor.GREEN + "Exited linear waypoint editor.");
                callback.onProviderChanged();
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player))
                    return;

                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    waypoints.add(new Waypoint(event.getClickedBlock().getLocation()));
                    Messaging.send(player, "<e>Added<a> a waypoint.");
                } else if (waypoints.size() > 0) {
                    waypoints.remove(waypoints.size() - 1);
                    Messaging.send(player,
                            String.format("<e>Removed<a> a waypoint (<e>%d<a> remaining)", waypoints.size()));
                }
            }
        };
    }

    @Override
    public NavigationCallback getCallback() {
        return callback;
    }

    @Override
    public Iterator<Waypoint> iterator() {
        return waypoints.iterator();
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("waypoints").getIntegerSubKeys()) {
            waypoints.add(new Waypoint(StorageUtils.loadLocation(root)));
        }
    }

    @Override
    public void save(DataKey key) {
        key = key.getRelative("waypoints");
        for (int i = 0; i < waypoints.size(); ++i) {
            StorageUtils.saveLocation(key.getRelative(Integer.toString(i)), waypoints.get(i).getLocation());
        }
    }
}
