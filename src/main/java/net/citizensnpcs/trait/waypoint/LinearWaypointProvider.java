package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
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
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL)
                    return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                    if (event.getClickedBlock() == null)
                        return;
                    Location at = event.getClickedBlock().getLocation();
                    waypoints.add(new Waypoint(at));
                    Messaging.send(player, String.format("<e>Added<a> a waypoint at (<e>%d<a>, <e>%d<a>, <e>%d<a>).",
                            at.getBlockX(), at.getBlockY(), at.getBlockZ()));
                } else if (waypoints.size() > 0) {
                    waypoints.remove(waypoints.size() - 1);
                    Messaging.send(player,
                            String.format("<e>Removed<a> a waypoint (<e>%d<a> remaining)", waypoints.size()));
                }
                callback.onProviderChanged();
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
    public void onAttach() {
        callback.onProviderChanged();
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("points").getIntegerSubKeys()) {
            root = root.getRelative("location");
            waypoints.add(new Waypoint(new Location(Bukkit.getWorld(root.getString("world")), root.getDouble("x"), root
                    .getDouble("y"), root.getDouble("z"), (float) root.getDouble("yaw", 0), (float) root.getDouble(
                    "pitch", 0))));
        }
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("points");
        key = key.getRelative("points");
        for (int i = 0; i < waypoints.size(); ++i) {
            Location location = waypoints.get(i).getLocation();
            DataKey root = key.getRelative(Integer.toString(i) + ".location");
            root.setString("world", location.getWorld().getName());
            root.setDouble("x", location.getX());
            root.setDouble("y", location.getY());
            root.setDouble("z", location.getZ());
            root.setDouble("yaw", location.getYaw());
            root.setDouble("pitch", location.getPitch());
        }
    }
}