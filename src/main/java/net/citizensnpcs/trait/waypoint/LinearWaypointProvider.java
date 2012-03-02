package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.StorageUtils;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
                Messaging.send(player, "<e>Left click<a> to add a waypoint, <e>right click<a> to remove.");
            }

            @Override
            public void end() {
                player.sendMessage(ChatColor.AQUA + "Exited the linear waypoint editor.");
                callback.onProviderChanged();
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player))
                    return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Location at = event.getClickedBlock().getLocation();
                    waypoints.add(new Waypoint(at));
                    Messaging.send(player, String.format("<e>Added<a> a waypoint at (<e>%d<a>, <e>%d<a>, <e>%d<a>).",
                            at.getBlockX(), at.getBlockY(), at.getBlockZ()));
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
        callback.onProviderChanged();
    }

    @Override
    public void save(DataKey key) {
        key = key.getRelative("waypoints");
        for (int i = 0; i < waypoints.size(); ++i) {
            StorageUtils.saveLocation(key.getRelative(Integer.toString(i)), waypoints.get(i).getLocation());
        }
    }
}
