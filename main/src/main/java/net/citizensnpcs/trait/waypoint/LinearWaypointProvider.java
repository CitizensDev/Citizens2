package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.waypoint.WaypointProvider.EnumerableWaypointProvider;
import net.citizensnpcs.trait.waypoint.triggers.TriggerEditPrompt;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class LinearWaypointProvider implements EnumerableWaypointProvider {
    private LinearWaypointGoal currentGoal;
    private NPC npc;
    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public WaypointEditor createEditor(CommandSender sender, CommandContext args) {
        if (args.hasFlag('h')) {
            try {
                if (args.getSenderLocation() != null) {
                    waypoints.add(new Waypoint(args.getSenderLocation()));
                }
            } catch (CommandException e) {
                Messaging.sendError(sender, e.getMessage());
            }
            return null;
        } else if (args.hasValueFlag("at")) {
            try {
                Location location = CommandContext.parseLocation(args.getSenderLocation(), args.getFlag("at"));
                if (location != null) {
                    waypoints.add(new Waypoint(location));
                }
            } catch (CommandException e) {
                Messaging.sendError(sender, e.getMessage());
            }
            return null;
        } else if (args.hasFlag('c')) {
            waypoints.clear();
            return null;
        } else if (args.hasFlag('l')) {
            if (waypoints.size() > 0) {
                waypoints.remove(waypoints.size() - 1);
            }
            return null;
        } else if (args.hasFlag('p')) {
            setPaused(!isPaused());
            return null;
        } else if (!(sender instanceof Player)) {
            Messaging.sendErrorTr(sender, Messages.COMMAND_MUST_BE_INGAME);
            return null;
        }
        return new LinearWaypointEditor((Player) sender);
    }

    public Waypoint getCurrentWaypoint() {
        if (currentGoal != null && currentGoal.currentDestination != null) {
            return currentGoal.currentDestination;
        }
        return null;
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
    public void onRemove() {
        npc.getDefaultGoalController().removeGoal(currentGoal);
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = new LinearWaypointGoal();
            npc.getDefaultGoalController().addGoal(currentGoal, 1);
        }
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("points");
        key = key.getRelative("points");
        for (int i = 0; i < waypoints.size(); ++i) {
            PersistenceLoader.save(waypoints.get(i), key.getRelative(i));
        }
    }

    @Override
    public void setPaused(boolean paused) {
        if (currentGoal != null) {
            currentGoal.setPaused(paused);
        }
    }

    @Override
    public Iterable<Waypoint> waypoints() {
        return waypoints;
    }

    private final class LinearWaypointEditor extends WaypointEditor {
        Conversation conversation;
        boolean editing = true;
        int editingSlot = waypoints.size() - 1;
        WaypointMarkers markers;
        private final Player player;
        private boolean showPath;

        private LinearWaypointEditor(Player player) {
            this.player = player;
            this.markers = new WaypointMarkers(player.getWorld());
        }

        @Override
        public void begin() {
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_BEGIN);
        }

        private void clearWaypoints() {
            editingSlot = 0;
            waypoints.clear();
            onWaypointsModified();
            markers.destroyWaypointMarkers();
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_WAYPOINTS_CLEARED);
        }

        private void createWaypointMarkers() {
            for (int i = 0; i < waypoints.size(); i++) {
                markers.createWaypointMarker(waypoints.get(i));
            }
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
            markers.destroyWaypointMarkers();
        }

        private String formatLoc(Location location) {
            return String.format("[[%d]], [[%d]], [[%d]]", location.getBlockX(), location.getBlockY(),
                    location.getBlockZ());
        }

        @Override
        public Waypoint getCurrentWaypoint() {
            if (waypoints.size() == 0 || !editing) {
                return null;
            }
            normaliseEditingSlot();
            return waypoints.get(editingSlot);
        }

        private Location getPreviousWaypoint(int fromSlot) {
            if (waypoints.size() <= 1)
                return null;
            if (--fromSlot < 0)
                fromSlot = waypoints.size() - 1;
            return waypoints.get(fromSlot).getLocation();
        }

        private void normaliseEditingSlot() {
            editingSlot = Math.max(0, Math.min(waypoints.size() - 1, editingSlot));
        }

        @EventHandler
        public void onNPCDespawn(NPCDespawnEvent event) {
            if (event.getNPC().equals(npc)) {
                Editor.leave(player);
            }
        }

        @EventHandler
        public void onNPCRemove(NPCRemoveEvent event) {
            if (event.getNPC().equals(npc)) {
                Editor.leave(player);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().equals(player))
                return;
            String message = event.getMessage();
            if (message.equalsIgnoreCase("triggers")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        conversation = TriggerEditPrompt.start(player, LinearWaypointEditor.this);
                    }
                });
            } else if (message.equalsIgnoreCase("clear")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        clearWaypoints();
                    }
                });
            } else if (message.equalsIgnoreCase("toggle path")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        // we need to spawn entities on the main thread.
                        togglePath();
                    }
                });
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL || !npc.isSpawned()
                    || event.getPlayer().getWorld() != npc.getEntity().getWorld()
                    || event.getHand() == EquipmentSlot.OFF_HAND)
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
                if (showPath) {
                    markers.createWaypointMarker(element);
                }
                editingSlot = Math.min(editingSlot + 1, waypoints.size());
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_ADDED_WAYPOINT, formatLoc(at), editingSlot + 1,
                        waypoints.size());
            } else if (waypoints.size() > 0) {
                event.setCancelled(true);
                normaliseEditingSlot();
                Waypoint waypoint = waypoints.remove(editingSlot);
                if (showPath) {
                    markers.removeWaypointMarker(waypoint);
                }
                editingSlot = Math.max(0, editingSlot - 1);
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_REMOVED_WAYPOINT, waypoints.size(),
                        editingSlot + 1);
            }
            onWaypointsModified();
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (!player.equals(event.getPlayer()) || !showPath || event.getHand() == EquipmentSlot.OFF_HAND)
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

        private void onWaypointsModified() {
            if (currentGoal != null) {
                currentGoal.onProviderChanged();
            }
        }

        private void togglePath() {
            showPath = !showPath;
            if (showPath) {
                createWaypointMarkers();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_SHOWING_MARKERS);
            } else {
                markers.destroyWaypointMarkers();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_NOT_SHOWING_MARKERS);
            }
        }

        private static final int LARGEST_SLOT = 8;
    }

    private class LinearWaypointGoal implements Goal {
        private final Location cachedLocation = new Location(null, 0, 0, 0);
        private Waypoint currentDestination;
        private Iterator<Waypoint> itr;
        private boolean paused;
        private GoalSelector selector;

        private void ensureItr() {
            if (itr == null) {
                itr = getUnsafeIterator();
            } else if (!itr.hasNext()) {
                itr = getNewIterator();
            }
        }

        private Navigator getNavigator() {
            return npc.getNavigator();
        }

        private Iterator<Waypoint> getNewIterator() {
            LinearWaypointsCompleteEvent event = new LinearWaypointsCompleteEvent(LinearWaypointProvider.this,
                    getUnsafeIterator());
            Bukkit.getPluginManager().callEvent(event);
            Iterator<Waypoint> next = event.getNextWaypoints();
            return next;
        }

        private Iterator<Waypoint> getUnsafeIterator() {
            return new Iterator<Waypoint>() {
                int idx = 0;

                @Override
                public boolean hasNext() {
                    return idx < waypoints.size();
                }

                @Override
                public Waypoint next() {
                    return waypoints.get(idx++);
                }

                @Override
                public void remove() {
                    waypoints.remove(Math.max(0, idx - 1));
                }
            };
        }

        public boolean isPaused() {
            return paused;
        }

        public void onProviderChanged() {
            itr = getUnsafeIterator();
            if (currentDestination != null) {
                if (selector != null) {
                    selector.finish();
                }
                if (npc != null && npc.getNavigator().isNavigating()) {
                    npc.getNavigator().cancelNavigation();
                }
            }
        }

        @Override
        public void reset() {
            currentDestination = null;
            selector = null;
        }

        @Override
        public void run(GoalSelector selector) {
            if (!getNavigator().isNavigating()) {
                selector.finish();
            }
        }

        public void setPaused(boolean pause) {
            if (pause && currentDestination != null) {
                selector.finish();
            }
            paused = pause;
        }

        @Override
        public boolean shouldExecute(final GoalSelector selector) {
            if (paused || currentDestination != null || !npc.isSpawned() || getNavigator().isNavigating()) {
                return false;
            }
            ensureItr();
            boolean shouldExecute = itr.hasNext();
            if (!shouldExecute) {
                return false;
            }
            this.selector = selector;
            Waypoint next = itr.next();
            Location npcLoc = npc.getEntity().getLocation(cachedLocation);
            if (npcLoc.getWorld() != next.getLocation().getWorld() || npcLoc.distanceSquared(next.getLocation()) < npc
                    .getNavigator().getLocalParameters().distanceMargin()) {
                return false;
            }
            currentDestination = next;
            getNavigator().setTarget(currentDestination.getLocation());
            getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(@Nullable CancelReason cancelReason) {
                    if (npc.isSpawned() && currentDestination != null
                            && Util.locationWithinRange(npc.getStoredLocation(), currentDestination.getLocation(), 2)) {
                        currentDestination.onReach(npc);
                    }
                    selector.finish();
                }
            });
            return true;
        }
    }
}
