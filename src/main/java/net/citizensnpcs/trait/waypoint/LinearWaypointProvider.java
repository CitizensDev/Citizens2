package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class LinearWaypointProvider implements WaypointProvider {
    private LinearWaypointGoal currentGoal;
    private NPC npc;
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
                return String.format("<e>%d<a>, <e>%d<a>, <e>%d<a>", location.getBlockX(),
                        location.getBlockY(), location.getBlockZ());
            }

            private Location getPreviousWaypoint(int fromSlot) {
                if (waypoints.size() <= 1)
                    return null;
                fromSlot--;
                if (fromSlot < 0)
                    fromSlot = waypoints.size() - 1;
                return waypoints.get(fromSlot).getLocation();
            }

            @EventHandler
            public void onNPCDespawn(NPCDespawnEvent event) {
                if (event.getNPC().equals(npc))
                    end();
            }

            @EventHandler
            public void onNPCRemove(NPCRemoveEvent event) {
                if (event.getNPC().equals(npc))
                    end();
            }

            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL)
                    return;
                if (event.getPlayer().getWorld() != npc.getBukkitEntity().getWorld())
                    return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK
                        || event.getAction() == Action.LEFT_CLICK_AIR) {
                    if (event.getClickedBlock() == null)
                        return;
                    event.setCancelled(true);
                    Location at = event.getClickedBlock().getLocation();
                    Location prev = getPreviousWaypoint(editingSlot);

                    if (prev != null) {
                        double distance = at.distanceSquared(prev);
                        double maxDistance = npc.getNavigator().getDefaultParameters().range();
                        maxDistance = Math.pow(maxDistance, 2);
                        if (distance > maxDistance) {
                            Messaging.sendF(player, ChatColor.RED
                                    + "Previous waypoint is %d blocks away but the distance limit is %d.",
                                    StringHelper.wrap(distance, ChatColor.RED),
                                    StringHelper.wrap(maxDistance, ChatColor.RED));
                            return;
                        }
                    }

                    waypoints.add(Math.max(0, editingSlot), new Waypoint(at));
                    editingSlot = Math.min(editingSlot + 1, waypoints.size());
                    Messaging.send(
                            player,
                            String.format("<e>Added<a> a waypoint at (" + formatLoc(at)
                                    + ") (<e>%d<a>, <e>%d<a>)", editingSlot + 1, waypoints.size()));
                } else if (waypoints.size() > 0) {
                    event.setCancelled(true);
                    editingSlot = Math.min(0, Math.max(waypoints.size() - 1, editingSlot));
                    waypoints.remove(editingSlot);
                    editingSlot = Math.max(0, editingSlot - 1);
                    Messaging.send(player, String.format(
                            "<e>Removed<a> a waypoint (<e>%d<a> remaining) (<e>%d<a>)", waypoints.size(),
                            editingSlot + 1));
                }
                currentGoal.onProviderChanged();
            }

            @EventHandler
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
    public boolean isPaused() {
        return currentGoal.isPaused();
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("points").getIntegerSubKeys()) {
            root = root.getRelative("location");
            waypoints.add(new Waypoint(new Location(Bukkit.getWorld(root.getString("world")), root
                    .getDouble("x"), root.getDouble("y"), root.getDouble("z"), (float) root.getDouble("yaw",
                    0), (float) root.getDouble("pitch", 0))));
        }
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = new LinearWaypointGoal();
            CitizensAPI.registerEvents(currentGoal);
            npc.getDefaultGoalController().addGoal(currentGoal, 1);
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

    @Override
    public void setPaused(boolean paused) {
        currentGoal.setPaused(paused);
    }

    private class LinearWaypointGoal implements Goal {
        private Location currentDestination;
        private Iterator<Location> itr;
        private boolean paused;
        private GoalSelector selector;

        private void ensureItr() {
            if (itr == null || !itr.hasNext())
                itr = Iterators.transform(waypoints.iterator(), WAYPOINT_TRANSFORMER);
        }

        private Navigator getNavigator() {
            return npc.getNavigator();
        }

        public boolean isPaused() {
            return paused;
        }

        @EventHandler
        public void onNavigationComplete(NavigationCompleteEvent event) {
            if (currentDestination == null || !event.getNavigator().equals(getNavigator()))
                return;
            if (currentDestination.equals(event.getNavigator().getTargetAsLocation()))
                selector.finish();
        }

        public void onProviderChanged() {
            itr = Iterators.transform(waypoints.iterator(), WAYPOINT_TRANSFORMER);
            if (currentDestination != null)
                selector.finish();
        }

        @Override
        public void reset() {
            currentDestination = null;
            selector = null;
        }

        @Override
        public void run(GoalSelector selector) {
        }

        public void setPaused(boolean pause) {
            if (pause && currentDestination != null)
                selector.finish();
            this.paused = pause;
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            if (paused || currentDestination != null || !npc.isSpawned() || waypoints.size() == 0)
                return false;
            if (waypoints.size() == 1) {
                // avoid pathing to the same point and wasting memory.
                Location dest = npc.getBukkitEntity().getLocation();
                if (waypoints.get(0).getLocation().distanceSquared(dest) < 3)
                    return false;
            }
            ensureItr();
            boolean shouldExecute = itr.hasNext();
            if (shouldExecute) {
                this.selector = selector;
                currentDestination = itr.next();
                getNavigator().setTarget(currentDestination);
            }
            return shouldExecute;
        }
    }

    private static final Function<Waypoint, Location> WAYPOINT_TRANSFORMER = new Function<Waypoint, Location>() {
        @Override
        public Location apply(@Nullable Waypoint input) {
            return input == null ? null : input.getLocation();
        }
    };
}