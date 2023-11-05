package net.citizensnpcs.trait.waypoint;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
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
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
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

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        if (currentGoal != null) {
            currentGoal.onProviderChanged();
        }
    }

    public boolean cachePaths() {
        return cachePaths;
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
            Messaging.sendErrorTr(sender, CommandMessages.MUST_BE_INGAME);
            return null;
        }
        return new LinearWaypointEditor((Player) sender);
    }

    public boolean cycleWaypoints() {
        return cycle;
    }

    public Waypoint getCurrentWaypoint() {
        if (currentGoal != null && currentGoal.currentDestination != null)
            return currentGoal.currentDestination;
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
            if (waypoint == null) {
                continue;
            }
            waypoints.add(waypoint);
        }
    }

    @Override
    public void onRemove() {
        if (currentGoal == null)
            return;
        currentGoal.onProviderChanged();
        npc.getDefaultGoalController().removeGoal(currentGoal);
        currentGoal = null;
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

    public void setCachePaths(boolean cachePaths) {
        this.cachePaths = cachePaths;
        if (currentGoal != null) {
            currentGoal.onProviderChanged();
        }
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
        if (currentGoal != null) {
            currentGoal.onProviderChanged();
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
        return new AbstractList<Waypoint>() {
            @Override
            public void add(int index, Waypoint waypoint) {
                waypoints.add(index, waypoint);
                mod();
            }

            @Override
            public boolean add(Waypoint waypoint) {
                boolean val = waypoints.add(waypoint);
                mod();
                return val;
            }

            @Override
            public void clear() {
                waypoints.clear();
                mod();
            }

            @Override
            public Waypoint get(int index) {
                return waypoints.get(index);
            }

            private void mod() {
                if (currentGoal != null) {
                    currentGoal.onProviderChanged();
                }
            }

            @Override
            public Waypoint remove(int index) {
                Waypoint val = waypoints.remove(index);
                mod();
                return val;
            }

            @Override
            public boolean remove(Object waypoint) {
                boolean val = waypoints.remove(waypoint);
                mod();
                return val;
            }

            @Override
            public Waypoint set(int index, Waypoint elem) {
                Waypoint val = waypoints.set(index, elem);
                mod();
                return val;
            }

            @Override
            public int size() {
                return waypoints.size();
            }
        };
    }

    private class LinearWaypointEditor extends WaypointEditor {
        Conversation conversation;
        boolean editing = true;
        EntityMarkers<Waypoint> markers;
        private final Player player;
        private Waypoint selectedWaypoint;
        private boolean showingMarkers = true;

        private LinearWaypointEditor(Player player) {
            this.player = player;
            markers = new EntityMarkers<>();
        }

        private void addWaypoint(Location at) {
            Waypoint element = new Waypoint(at);
            int idx = waypoints.size();
            if (waypoints.indexOf(selectedWaypoint) != -1) {
                idx = waypoints.indexOf(selectedWaypoint);
                waypoints.add(idx, element);
            } else {
                waypoints.add(element);
            }
            if (showingMarkers) {
                markers.createMarker(element, element.getLocation().clone());
            }
            Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_ADDED_WAYPOINT, formatLoc(at), waypoints.size());
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
                markers.createMarker(waypoints.get(i), waypoints.get(i).getLocation());
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
            if (waypoints.size() == 0 || !editing)
                return null;

            return selectedWaypoint == null ? waypoints.get(waypoints.size() - 1) : selectedWaypoint;
        }

        private Location getLastWaypoint() {
            if (waypoints.size() <= 1)
                return null;
            return waypoints.get(waypoints.size() - 1).getLocation();
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                    conversation = TriggerEditPrompt.start(player, LinearWaypointEditor.this);
                    conversation.addConversationAbandonedListener(e -> {
                        setPaused(false);
                        conversation = null;
                    });
                    setPaused(true);
                });
            } else if (message.equalsIgnoreCase("clear")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), this::clearWaypoints);
            } else if (message.equalsIgnoreCase("toggle path") || message.equalsIgnoreCase("markers")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), this::togglePath);
            } else if (message.equalsIgnoreCase("cycle")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                    cycle = !cycle;
                    Messaging.sendTr(event.getPlayer(), cycle ? Messages.LINEAR_WAYPOINT_EDITOR_CYCLE_SET
                            : Messages.LINEAR_WAYPOINT_EDITOR_CYCLE_UNSET);
                });
            } else if (message.equalsIgnoreCase("here")) {
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                        () -> addWaypoint(player.getLocation()));
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
                Location at = event.getClickedBlock().getLocation().add(0, 1, 0);
                Location prev = getLastWaypoint();

                if (prev != null && prev.getWorld() == at.getWorld()) {
                    double distance = at.distance(prev);
                    double maxDistance = npc.getNavigator().getDefaultParameters().range();
                    if (distance > maxDistance) {
                        Messaging.sendErrorTr(player, Messages.LINEAR_WAYPOINT_EDITOR_RANGE_EXCEEDED, distance,
                                maxDistance, ChatColor.RED);
                        return;
                    }
                }
                addWaypoint(at);
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
        public boolean shouldExecute(GoalSelector selector) {
            if (paused || currentDestination != null || !npc.isSpawned() || getNavigator().isNavigating())
                return false;

            ensureItr();
            boolean shouldExecute = itr.hasNext();
            if (!shouldExecute)
                return false;

            this.selector = selector;
            Waypoint next = itr.next();
            Location npcLoc = npc.getEntity().getLocation(cachedLocation);
            if (npcLoc.getWorld() != next.getLocation().getWorld()
                    || npcLoc.distance(next.getLocation()) <= npc.getNavigator().getLocalParameters().distanceMargin())
                return false;

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
            double margin = getNavigator().getLocalParameters().distanceMargin();
            getNavigator().getLocalParameters().addSingleUseCallback(cancelReason -> {
                if (npc.isSpawned() && currentDestination != null && Util.locationWithinRange(npc.getStoredLocation(),
                        currentDestination.getLocation(), margin + 1)) {
                    currentDestination.onReach(npc);
                    if (cachePaths && cancelReason == null) {
                        Iterable<Vector> path = getNavigator().getPathStrategy().getPath();
                        if (Iterables.size(path) > 0) {
                            cachedPaths.put(new SourceDestinationPair(npcLoc, currentDestination), path);
                        }
                    }
                }
                selector.finish();
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
            if (this == obj)
                return true;

            if (obj == null || getClass() != obj.getClass())
                return false;

            SourceDestinationPair other = (SourceDestinationPair) obj;
            if (!Objects.equals(from, other.from) || !Objects.equals(to, other.to))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = prime + (from == null ? 0 : from.hashCode());
            return prime * result + (to == null ? 0 : to.hashCode());
        }

        public boolean verify(World world, Iterable<Vector> cached) {
            for (Vector vector : cached) {
                if (!MinecraftBlockExaminer
                        .canStandOn(world.getBlockAt(vector.getBlockX(), vector.getBlockY() - 1, vector.getBlockZ())))
                    return false;
            }
            return true;
        }
    }
}
