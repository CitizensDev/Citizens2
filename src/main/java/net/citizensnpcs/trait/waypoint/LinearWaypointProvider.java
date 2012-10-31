package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.waypoint.triggers.TriggerEditPrompt;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMS;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.conversations.Conversation;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LinearWaypointProvider implements WaypointProvider {
    private LinearWaypointGoal currentGoal;
    private NPC npc;
    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public WaypointEditor createEditor(Player player) {
        return new LinearWaypointEditor(player);
    }

    @Override
    public boolean isPaused() {
        return currentGoal.isPaused();
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("points").getIntegerSubKeys()) {
            Waypoint waypoint = PersistenceLoader.load(Waypoint.class, root);
            if (waypoint == null)
                continue;
            waypoints.add(waypoint);
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
        for (int i = 0; i < waypoints.size(); ++i)
            PersistenceLoader.save(waypoints.get(i), key.getRelative(i));
    }

    @Override
    public void setPaused(boolean paused) {
        currentGoal.setPaused(paused);
    }

    private final class LinearWaypointEditor extends WaypointEditor {
        Conversation conversation;
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
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_BEGIN);
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
            if (conversation != null)
                conversation.abandon();
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_END);
            editing = false;
            if (!showPath)
                return;
            destroyWaypointMarkers();
        }

        private String formatLoc(Location location) {
            return String.format("[[%d]], [[%d]], [[%d]]", location.getBlockX(), location.getBlockY(),
                    location.getBlockZ());
        }

        @Override
        public Waypoint getCurrentWaypoint() {
            if (waypoints.size() == 0 || !editing)
                return null;
            normaliseEditingSlot();
            return waypoints.get(editingSlot);
        }

        private Location getPreviousWaypoint(int fromSlot) {
            if (waypoints.size() <= 1)
                return null;
            fromSlot--;
            if (fromSlot < 0)
                fromSlot = waypoints.size() - 1;
            return waypoints.get(fromSlot).getLocation();
        }

        private void normaliseEditingSlot() {
            editingSlot = Math.max(0, Math.min(waypoints.size() - 1, editingSlot));
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
            if (event.getMessage().equalsIgnoreCase("triggers")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        conversation = TriggerEditPrompt.start(player, LinearWaypointEditor.this);
                    }
                });
            }
            if (!event.getMessage().equalsIgnoreCase("toggle path"))
                return;
            event.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    // we need to spawn entities, get back on the main thread.
                    togglePath();
                }
            });
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
                        Messaging.sendErrorTr(player, Messages.LINEAR_WAYPOINT_EDITOR_RANGE_EXCEEDED,
                                Math.sqrt(distance), Math.sqrt(maxDistance), ChatColor.RED);
                        return;
                    }
                }

                Waypoint element = new Waypoint(at);
                normaliseEditingSlot();
                waypoints.add(editingSlot, element);
                if (showPath)
                    createWaypointMarker(editingSlot, element);
                editingSlot = Math.min(editingSlot + 1, waypoints.size());
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_ADDED_WAYPOINT, formatLoc(at),
                        editingSlot + 1, waypoints.size());
            } else if (waypoints.size() > 0) {
                event.setCancelled(true);
                normaliseEditingSlot();
                Waypoint waypoint = waypoints.remove(editingSlot);
                if (showPath)
                    removeWaypointMarker(waypoint);
                editingSlot = Math.max(0, editingSlot - 1);
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_REMOVED_WAYPOINT, waypoints.size(),
                        editingSlot + 1);
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
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_EDIT_SLOT_SET, editingSlot,
                    formatLoc(waypoints.get(editingSlot).getLocation()));
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
            normaliseEditingSlot();
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_EDIT_SLOT_SET, editingSlot,
                    formatLoc(waypoints.get(editingSlot).getLocation()));
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
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_SHOWING_MARKERS);
            } else {
                destroyWaypointMarkers();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_NOT_SHOWING_MARKERS);
            }
        }

        private static final int LARGEST_SLOT = 8;
    }

    private class LinearWaypointGoal implements Goal {
        private Waypoint currentDestination;
        private Iterator<Waypoint> itr;
        private boolean paused;
        private GoalSelector selector;

        private void ensureItr() {
            if (itr == null || !itr.hasNext())
                itr = waypoints.iterator();
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
            Waypoint from = currentDestination;
            selector.finish();
            Location finished = event.getNavigator().getTargetAsLocation();
            if (finished == null || from == null)
                return;
            if (finished.getWorld() != from.getLocation().getWorld())
                return;
            if (finished.equals(from.getLocation()))
                from.onReach(npc);
        }

        public void onProviderChanged() {
            itr = waypoints.iterator();
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
                // avoid pathing to the same point repeatedly
                Location dest = npc.getBukkitEntity().getLocation();
                if (waypoints.get(0).getLocation().distanceSquared(dest) < 3)
                    return false;
            }
            ensureItr();
            boolean shouldExecute = itr.hasNext();
            if (shouldExecute) {
                this.selector = selector;
                currentDestination = itr.next();
                getNavigator().setTarget(currentDestination.getLocation());
            }
            return shouldExecute;
        }
    }
}