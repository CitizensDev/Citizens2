package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.waypoint.WaypointProvider.EnumerableWaypointProvider;
import net.citizensnpcs.trait.waypoint.triggers.TriggerEditPrompt;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

/**
 * An ordered list of {@link Waypoint}s to walk between.
 */
public class LinearWaypointProvider implements EnumerableWaypointProvider {
    private final Map<SourceDestinationPair, Iterable<Vector>> cachedPaths = Maps.newHashMap();
    @Persist
    private boolean cachePaths = Setting.DEFAULT_CACHE_WAYPOINT_PATHS.asBoolean();
    private LinearWaypointGoal currentGoal;
    @Persist
    private boolean cycle = false;
    private NPC npc;
    private final List<Waypoint> waypoints = Lists.newArrayList();

    public LinearWaypointProvider() {
    }

    public LinearWaypointProvider(NPC npc) {
        this.npc = npc;
    }

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
            cachedPaths.clear();
            return null;
        } else if (args.hasFlag('l')) {
            if (waypoints.size() > 0) {
                waypoints.remove(waypoints.size() - 1);
            }
            return null;
        } else if (args.hasFlag('p')) {
            setPaused(!isPaused());
            return null;
        } else if (args.hasFlag('k')) {
            cachePaths = !cachePaths;
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
        return currentGoal == null ? false : currentGoal.isPaused();
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
        if (currentGoal != null) {
            npc.getDefaultGoalController().removeGoal(currentGoal);
            currentGoal = null;
        }
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
        DataKey root = key.getRelative("points");
        for (int i = 0; i < waypoints.size(); ++i) {
            PersistenceLoader.save(waypoints.get(i), root.getRelative(i));
        }
    }

    @Override
    public void setPaused(boolean paused) {
        if (currentGoal != null) {
            currentGoal.setPaused(paused);
        }
    }

    /**
     * Returns the modifiable list of waypoints.
     */
    @Override
    public Iterable<Waypoint> waypoints() {
        return waypoints;
    }

    private final class LinearWaypointEditor extends WaypointEditor {
        Conversation conversation;
        boolean editing = true;
        EntityMarkers<Waypoint> markers;
        private final Player player;
        private Waypoint selectedWaypoint;
        private boolean showingMarkers = true;

        private LinearWaypointEditor(Player player) {
            this.player = player;
            this.markers = new EntityMarkers<Waypoint>();
        }

        @Override
        public void begin() {
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_BEGIN);
            if (showingMarkers) {
                createWaypointMarkers();
            }
        }

        private void clearWaypoints() {
            waypoints.clear();
            onWaypointsModified();
            markers.destroyMarkers();
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_WAYPOINTS_CLEARED);
        }

        private void createWaypointMarkers() {
            for (int i = 0; i < waypoints.size(); i++) {
                markers.createMarker(waypoints.get(i), waypoints.get(i).getLocation().clone().add(0, 1, 0));
            }
        }

        @Override
        public void end() {
            if (!editing)
                return;
            editing = false;
            if (conversation != null) {
                conversation.abandon();
            }
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_END);
            markers.destroyMarkers();
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
            return selectedWaypoint == null ? waypoints.get(waypoints.size() - 1) : selectedWaypoint;
        }

        private Location getPreviousWaypoint() {
            if (waypoints.size() <= 1)
                return null;
            return waypoints.get(waypoints.size() - 2).getLocation();
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
                        conversation.addConversationAbandonedListener(new ConversationAbandonedListener() {
                            @Override
                            public void conversationAbandoned(ConversationAbandonedEvent event) {
                                conversation = null;
                            }
                        });
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
            } else if (message.equalsIgnoreCase("toggle path") || message.equalsIgnoreCase("markers")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        togglePath();
                    }
                });
            } else if (message.equalsIgnoreCase("cycle")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        cycle = !cycle;
                        Messaging.sendTr(event.getPlayer(), cycle ? Messages.LINEAR_WAYPOINT_EDITOR_CYCLE_SET
                                : Messages.LINEAR_WAYPOINT_EDITOR_CYCLE_UNSET);
                    }
                });
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL || !npc.isSpawned()
                    || event.getPlayer().getWorld() != npc.getEntity().getWorld() || Util.isOffHand(event))
                return;
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                if (event.getClickedBlock() == null)
                    return;
                event.setCancelled(true);
                Location at = event.getClickedBlock().getLocation();
                Location prev = getPreviousWaypoint();

                if (prev != null && prev.getWorld() == at.getWorld()) {
                    double distance = at.distance(prev);
                    double maxDistance = npc.getNavigator().getDefaultParameters().range();
                    if (distance > maxDistance) {
                        Messaging.sendErrorTr(player, Messages.LINEAR_WAYPOINT_EDITOR_RANGE_EXCEEDED, distance,
                                maxDistance, ChatColor.RED);
                        return;
                    }
                }

                Waypoint element = new Waypoint(at);
                waypoints.add(element);
                if (showingMarkers) {
                    markers.createMarker(element, element.getLocation().clone().add(0, 1, 0));
                }
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_ADDED_WAYPOINT, formatLoc(at),
                        waypoints.size());
            } else if (waypoints.size() > 0 && !event.getPlayer().isSneaking()) {
                event.setCancelled(true);

                Waypoint waypoint = removeWaypoint(waypoints.size() - 1);
                if (waypoint.equals(selectedWaypoint)) {
                    selectedWaypoint = null;
                }
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_REMOVED_WAYPOINT, waypoints.size());
            }
            onWaypointsModified();
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            if (!player.equals(event.getPlayer()) || !showingMarkers || Util.isOffHand(event))
                return;
            int slot = -1;
            double minDistance = Double.MAX_VALUE;
            for (int i = 0; i < waypoints.size(); i++) {
                Waypoint waypoint = waypoints.get(i);
                double distance = waypoint.getLocation()
                        .distanceSquared(event.getRightClicked().getLocation().add(0, -1, 0));
                if (minDistance > distance) {
                    minDistance = distance;
                    slot = i;
                }
            }
            if (slot == -1)
                return;
            if (selectedWaypoint != null && waypoints.get(slot) == selectedWaypoint) {
                removeWaypoint(slot);
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_REMOVED_WAYPOINT, waypoints.size());
                return;
            }
            selectedWaypoint = waypoints.get(slot);
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_SELECTED_WAYPOINT,
                    formatLoc(selectedWaypoint.getLocation()));
        }

        private void onWaypointsModified() {
            if (currentGoal != null) {
                currentGoal.onProviderChanged();
            }
            if (conversation != null && getCurrentWaypoint() != null) {
                getCurrentWaypoint().describeTriggers(player);
            }
        }

        private Waypoint removeWaypoint(int idx) {
            Waypoint waypoint = waypoints.remove(idx);
            if (showingMarkers) {
                markers.removeMarker(waypoint);
            }
            if (waypoint == selectedWaypoint) {
                selectedWaypoint = null;
            }
            return waypoint;
        }

        private void togglePath() {
            showingMarkers = !showingMarkers;
            if (showingMarkers) {
                createWaypointMarkers();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_SHOWING_MARKERS);
            } else {
                markers.destroyMarkers();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_NOT_SHOWING_MARKERS);
            }
        }
    }

    private class LinearWaypointGoal implements Goal {
        private boolean ascending = true;
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
            if (cycle && ascending) {
                ascending = false;
                return new Iterator<Waypoint>() {
                    int idx = waypoints.size() - 1;

                    @Override
                    public boolean hasNext() {
                        return idx >= 0 && idx < waypoints.size();
                    }

                    @Override
                    public Waypoint next() {
                        return waypoints.get(idx--);
                    }

                    @Override
                    public void remove() {
                        waypoints.remove(Math.max(0, idx - 1));
                    }
                };
            } else {
                ascending = true;
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
            paused = pause;
            if (pause && currentDestination != null) {
                selector.finish();
                if (npc != null && npc.getNavigator().isNavigating()) {
                    npc.getNavigator().cancelNavigation();
                }
            }
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
            final Location npcLoc = npc.getEntity().getLocation(cachedLocation);
            if (npcLoc.getWorld() != next.getLocation().getWorld() || npcLoc.distanceSquared(next.getLocation()) < npc
                    .getNavigator().getLocalParameters().distanceMargin()) {
                return false;
            }
            currentDestination = next;
            if (cachePaths) {
                SourceDestinationPair key = new SourceDestinationPair(npcLoc, currentDestination);
                Iterable<Vector> cached = cachedPaths.get(key);
                if (cached != null) {
                    if (Iterables.size(cached) == 0 || !key.verify(npcLoc.getWorld(), cached)) {
                        cachedPaths.remove(key);
                    } else {
                        getNavigator().setTarget(cached);
                    }
                }
            }
            if (!getNavigator().isNavigating()) {
                getNavigator().setTarget(currentDestination.getLocation());
            }
            getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(@Nullable CancelReason cancelReason) {
                    if (npc.isSpawned() && currentDestination != null
                            && Util.locationWithinRange(npc.getStoredLocation(), currentDestination.getLocation(),
                                    Setting.DEFAULT_DISTANCE_MARGIN.asDouble() + 1)) {
                        currentDestination.onReach(npc);
                        if (cachePaths && cancelReason == null) {
                            Iterable<Vector> path = getNavigator().getPathStrategy().getPath();
                            if (Iterables.size(path) > 0) {
                                cachedPaths.put(new SourceDestinationPair(npcLoc, currentDestination), path);
                            }
                        }
                    }
                    selector.finish();
                }
            });
            return true;
        }
    }

    private static class SourceDestinationPair {
        private final Vector from;
        private final Vector to;

        public SourceDestinationPair(Location npcLoc, Waypoint to) {
            this(new Vector(npcLoc.getBlockX(), npcLoc.getBlockY(), npcLoc.getBlockZ()), to.getLocation().toVector());
        }

        public SourceDestinationPair(Vector from, Vector to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SourceDestinationPair other = (SourceDestinationPair) obj;
            if (from == null) {
                if (other.from != null) {
                    return false;
                }
            } else if (!from.equals(other.from)) {
                return false;
            }
            if (to == null) {
                if (other.to != null) {
                    return false;
                }
            } else if (!to.equals(other.to)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = prime + ((from == null) ? 0 : from.hashCode());
            return prime * result + ((to == null) ? 0 : to.hashCode());
        }

        public boolean verify(World world, Iterable<Vector> cached) {
            for (Vector vector : cached) {
                if (!MinecraftBlockExaminer
                        .validPosition(world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()))) {
                    return false;
                }
            }
            return true;
        }
    }
}
