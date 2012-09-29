package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LinearWaypointProvider implements WaypointProvider {
    private LinearWaypointGoal currentGoal;
    private NPC npc;
    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public Editor createEditor(Player player) {
        return new LinearWaypointEditor(player);
    }

    @Override
    public boolean isPaused() {
        return currentGoal.isPaused();
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("points").getIntegerSubKeys()) {
            root = root.getRelative("location");
            if (Bukkit.getWorld(root.getString("world")) == null)
                continue;
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

    private final class LinearWaypointEditor extends Editor {
        boolean editing = true;
        int editingSlot = waypoints.size() - 1;
        private final Player player;
        private boolean showPath;
        Map<Waypoint, Entity> waypointMarkers = Maps.newHashMap();
        private LinearWaypointEditor(Player player) {
            this.player = player;
        }

        @Override
        public void begin() {
            Messaging.send(player, ChatColor.AQUA + "Entered the linear waypoint editor!");
            Messaging.send(player, "<e>Left click<a> to add a waypoint, <e>right click<a> to remove.");
            Messaging.send(player, "<a>Type <e>toggle path<a> to toggle showing entities at waypoints.");
        }

        private void createWaypointMarker(int index, Waypoint waypoint) {
            Entity entity = spawnMarker(player.getWorld(), waypoint.getLocation().add(0, 1, 0));
            if (entity == null)
                return;
            entity.setMetadata("waypointindex", new FixedMetadataValue(CitizensAPI.getPlugin(), index));
            waypointMarkers.put(waypoint, entity);
        }

        private void createWaypointMarkers() {
            for (int i = 0; i < waypoints.size(); i++)
                createWaypointMarker(i, waypoints.get(i));
        }

        private void destroyWaypointMarkers() {
            for (Entity entity : waypointMarkers.values())
                entity.remove();
            waypointMarkers.clear();
        }

        @Override
        public void end() {
            if (!editing)
                return;
            Messaging.send(player, ChatColor.AQUA + "Exited the linear waypoint editor.");
            editing = false;
            if (!showPath)
                return;
            destroyWaypointMarkers();
        }

        private String formatLoc(Location location) {
            return String.format("<e>%d<a>, <e>%d<a>, <e>%d<a>", location.getBlockX(), location.getBlockY(),
                    location.getBlockZ());
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
                Editor.leave(player);
        }

        @EventHandler
        public void onNPCRemove(NPCRemoveEvent event) {
            if (event.getNPC().equals(npc))
                Editor.leave(player);
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().equals(player))
                return;
            if (!event.getMessage().equalsIgnoreCase("toggle path"))
                return;
            event.setCancelled(true);
            // we need to spawn entities, get back on the main thread.
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    togglePath();
                }
            }, 1);
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL)
                return;
            if (event.getPlayer().getWorld() != npc.getBukkitEntity().getWorld())
                return;
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                if (event.getClickedBlock() == null)
                    return;
                event.setCancelled(true);
                Location at = event.getClickedBlock().getLocation();
                Location prev = getPreviousWaypoint(editingSlot);

                if (prev != null) {
                    double distance = at.distanceSquared(prev);
                    double maxDistance = Math.pow(npc.getNavigator().getDefaultParameters().range(), 2);
                    if (distance > maxDistance) {
                        Messaging.sendF(player, ChatColor.RED
                                + "Previous waypoint is %s blocks away but the distance limit is %s.",
                                StringHelper.wrap(Math.sqrt(distance), ChatColor.RED),
                                StringHelper.wrap(Math.sqrt(maxDistance), ChatColor.RED));
                        return;
                    }
                }

                Waypoint element = new Waypoint(at);
                waypoints.add(Math.max(0, editingSlot), element);
                if (showPath)
                    createWaypointMarker(editingSlot, element);
                editingSlot = Math.min(editingSlot + 1, waypoints.size());
                Messaging.send(
                        player,
                        String.format("<e>Added<a> a waypoint at (" + formatLoc(at)
                                + ") (<e>%d<a>, <e>%d<a>)", editingSlot + 1, waypoints.size()));
            } else if (waypoints.size() > 0) {
                event.setCancelled(true);
                editingSlot = Math.min(0, Math.max(waypoints.size() - 1, editingSlot));
                Waypoint waypoint = waypoints.remove(editingSlot);
                if (showPath)
                    removeWaypointMarker(waypoint);
                editingSlot = Math.max(0, editingSlot - 1);
                Messaging.send(
                        player,
                        String.format("<e>Removed<a> a waypoint (<e>%d<a> remaining) (<e>%d<a>)",
                                waypoints.size(), editingSlot + 1));
            }
            currentGoal.onProviderChanged();
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (!player.equals(event.getPlayer()) || !showPath)
                return;
            if (!event.getRightClicked().hasMetadata("waypointindex"))
                return;
            editingSlot = event.getRightClicked().getMetadata("waypointindex").get(0).asInt();
            Messaging.sendF(player, ChatColor.GREEN + "Editing slot set to %s.",
                    StringHelper.wrap(editingSlot));
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

        private void removeWaypointMarker(Waypoint waypoint) {
            Entity entity = waypointMarkers.remove(waypoint);
            if (entity != null)
                entity.remove();
        }

        private Entity spawnMarker(World world, Location at) {
            return NMS.spawnCustomEntity(world, at, EntityEnderCrystalMarker.class, EntityType.ENDER_CRYSTAL);
        }

        private void togglePath() {
            showPath = !showPath;
            if (showPath) {
                createWaypointMarkers();
                Messaging.sendF(player, "%s waypoint markers.", StringHelper.wrap("Showing"));
            } else {
                destroyWaypointMarkers();
                Messaging.sendF(player, "%s showing waypoint markers.", StringHelper.wrap("Stopped"));
            }
        }

        private static final int LARGEST_SLOT = 8;
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
            if (selector == null || !event.getNavigator().equals(getNavigator()))
                return;
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
            if (!getNavigator().isNavigating())
                selector.finish();
        }

        public void setPaused(boolean pause) {
            if (pause && currentDestination != null)
                selector.finish();
            this.paused = pause;
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            if (paused || currentDestination != null || !npc.isSpawned() || getNavigator().isNavigating()
                    || waypoints.size() == 0) {
                return false;
            }
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