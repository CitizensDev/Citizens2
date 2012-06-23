package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.Location;

import net.citizensnpcs.api.abstraction.EventHandler;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.google.common.collect.Lists;

public class LinearWaypointProvider implements WaypointProvider, Iterable<Waypoint> {
    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public Editor createEditor(final Player player) {
        return new Editor() {
            int editingSlot = waypoints.size() - 1;

            @Override
            public void begin() {
                player.sendMessage(ChatColor.AQUA + "Entered the linear waypoint editor!");
                Messaging.send(player, "<e>Left click<a> to add a waypoint, <e>right click<a> to remove.");
            }

            @Override
            public void end() {
                player.sendMessage(ChatColor.AQUA + "Exited the linear waypoint editor.");
            }

            private String formatLoc(Location location) {
                return String.format("<e>%d<a>, <e>%d<a>, <e>%d<a>", location.getBlockX(), location.getBlockY(),
                        location.getBlockZ());
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
                    waypoints.add(Math.max(0, editingSlot), new Waypoint(at));
                    editingSlot = Math.min(editingSlot + 1, waypoints.size());
                    Messaging.send(player, String.format("<e>Added<a> a waypoint at (" + formatLoc(at)
                            + ") (<e>%d<a>, <e>%d<a>)", editingSlot + 1, waypoints.size()));
                } else if (waypoints.size() > 0) {
                    editingSlot = Math.min(0, Math.max(waypoints.size() - 1, editingSlot));
                    // normalise editing slot.
                    waypoints.remove(editingSlot);
                    editingSlot = Math.max(0, editingSlot - 1);
                    Messaging.send(player, String.format("<e>Removed<a> a waypoint (<e>%d<a> remaining) (<e>%d<a>)",
                            waypoints.size(), editingSlot + 1));
                }
                callback.onProviderChanged();
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
                if (!event.getPlayer().equals(player) || waypoints.size() == 0)
                    return;
                int previousSlot = event.getPreviousSlot(), newSlot = event.getNewSlot();
                // handle wrap-arounds
                if (previousSlot == 0 && newSlot == LARGEST_SLOT) {
                    editingSlot--;
                } else if (previousSlot == LARGEST_SLOT && newSlot == 0) {
                    editingSlot++;
                } else {
                    int diff = newSlot - previousSlot;
                    if (Math.abs(diff) != 1)
                        return; // the player isn't scrolling
                    editingSlot += diff > 0 ? 1 : -1;
                }
                if (editingSlot >= waypoints.size())
                    editingSlot = 0;
                if (editingSlot < 0)
                    editingSlot = waypoints.size() - 1;
                Messaging.send(player, "<a>Editing slot set to " + StringHelper.wrap(editingSlot) + " ("
                        + formatLoc(waypoints.get(editingSlot).getLocation()) + ").");
            }

            private static final int LARGEST_SLOT = 8;
        };
    }

    @Override
    public Iterator<Waypoint> iterator() {
        return waypoints.iterator();
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
    public void onAttach() {
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("points");
        key = key.getRelative("points");
        for (int i = 0; i < waypoints.size(); ++i) {
            WorldVector location = waypoints.get(i).getLocation();
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