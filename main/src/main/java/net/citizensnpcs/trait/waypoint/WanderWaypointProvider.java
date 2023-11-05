package net.citizensnpcs.trait.waypoint;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;

import ch.ethz.globis.phtree.PhTreeSolid;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController.GoalEntry;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

/**
 * A wandering waypoint provider that wanders between either a box centered at the current location or inside a region
 * defined by a list of boxes.
 */
public class WanderWaypointProvider implements WaypointProvider {
    private WanderGoal currentGoal;
    @Persist
    private int delay = -1;
    private NPC npc;
    @Persist
    private boolean pathfind = true;
    private boolean paused;
    @Persist
    private final List<Location> regionCentres = Lists.newArrayList();
    private PhTreeSolid<Boolean> tree = PhTreeSolid.create(3);
    @Persist
    private String worldguardRegion;
    private Object worldguardRegionCache;
    @Persist
    private int xrange = DEFAULT_XRANGE;
    @Persist
    private int yrange = DEFAULT_YRANGE;

    public void addRegionCentre(Location centre) {
        regionCentres.add(centre);
        recalculateTree();
    }

    public void addRegionCentres(Collection<Location> centre) {
        regionCentres.addAll(centre);
        recalculateTree();
    }

    @Override
    public WaypointEditor createEditor(CommandSender sender, CommandContext args) {
        return new WaypointEditor() {
            boolean editingRegions = false;
            EntityMarkers<Location> markers = new EntityMarkers<>();

            @Override
            public void begin() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_BEGIN, pathfind ? "<green>" : "<red>");
                setPaused(true);
            }

            @Override
            public void end() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_END);
                editingRegions = false;
                setPaused(false);
                markers.destroyMarkers();
            }

            private String formatLoc(Location location) {
                return String.format("[[%d]], [[%d]], [[%d]]", location.getBlockX(), location.getBlockY(),
                        location.getBlockZ());
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                if (!event.getPlayer().equals(sender))
                    return;
                String message = event.getMessage().toLowerCase();
                if (message.startsWith("xrange") || message.startsWith("yrange")) {
                    event.setCancelled(true);
                    int range = 0;
                    try {
                        range = Integer.parseInt(message.split(" ", 2)[1]);
                        if (range <= 0) {
                            range = 0;
                        }
                        if (message.startsWith("xrange")) {
                            setXYRange(range, yrange);
                        } else {
                            setXYRange(xrange, range);
                        }
                        recalculateTree();
                    } catch (NumberFormatException ex) {
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                            () -> Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_RANGE_SET, xrange, yrange));
                } else if (message.startsWith("regions")) {
                    event.setCancelled(true);
                    editingRegions = !editingRegions;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                        if (editingRegions) {
                            for (Location regionCentre : regionCentres) {
                                Entity entity = markers.createMarker(regionCentre, regionCentre);
                                entity.setMetadata("wandermarker",
                                        new FixedMetadataValue(CitizensAPI.getPlugin(), regionCentre));
                            }
                            Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_REGION_EDITING_START);
                        } else {
                            markers.destroyMarkers();
                            Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_REGION_EDITING_STOP);
                        }
                    });
                } else if (message.startsWith("delay")) {
                    event.setCancelled(true);
                    setDelay(Util.parseTicks(message.split(" ")[1]));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                            () -> Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_DELAY_SET, delay));
                } else if (message.startsWith("worldguardregion")) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                        Object region = null;
                        String regionId = message.replace("worldguardregion", "").trim();
                        try {
                            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                                    .get(BukkitAdapter.adapt(npc.getStoredLocation().getWorld()));
                            region = manager.getRegion(regionId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (region == null) {
                            Messaging.sendErrorTr(sender, Messages.WANDER_WAYPOINTS_WORLDGUARD_REGION_NOT_FOUND);
                            return;
                        }
                        setWorldGuardRegion(regionId);
                        Messaging.sendErrorTr(sender, Messages.WANDER_WAYPOINTS_WORLDGUARD_REGION_SET, regionId);
                    });
                } else if (message.startsWith("pathfind")) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                        pathfind = !pathfind;
                        if (currentGoal != null) {
                            currentGoal.setPathfind(pathfind);
                        }
                        begin();
                    });
                }
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(sender) || event.getAction() == Action.PHYSICAL || !npc.isSpawned()
                        || !editingRegions || event.getPlayer().getWorld() != npc.getEntity().getWorld()
                        || Util.isOffHand(event))
                    return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                    if (event.getClickedBlock() == null)
                        return;
                    event.setCancelled(true);
                    Location at = event.getClickedBlock().getLocation().add(0, 1, 0);
                    if (!regionCentres.contains(at)) {
                        regionCentres.add(at);
                        Entity entity = markers.createMarker(at, at);
                        entity.setMetadata("wandermarker", new FixedMetadataValue(CitizensAPI.getPlugin(), at));
                        Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_ADDED_REGION, formatLoc(at),
                                regionCentres.size());
                        recalculateTree();
                    }
                }
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                if (!sender.equals(event.getPlayer()) || !editingRegions || Util.isOffHand(event)
                        || !event.getRightClicked().hasMetadata("wandermarker"))
                    return;
                regionCentres.remove(event.getRightClicked().getMetadata("wandermarker").get(0).value());
                markers.removeMarker((Location) event.getRightClicked().getMetadata("wandermarker").get(0).value());
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_REMOVED_REGION,
                        formatLoc((Location) event.getRightClicked().getMetadata("wandermarker").get(0).value()),
                        regionCentres.size());
                recalculateTree();
            }

        };
    }

    public int getDelay() {
        return delay;
    }

    public List<Location> getRegionCentres() {
        return new RecalculateList();
    }

    public Object getWorldGuardRegion() {
        if (worldguardRegion == null)
            return null;

        if (worldguardRegionCache != null)
            return worldguardRegionCache;

        try {
            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(npc.getStoredLocation().getWorld()));
            return worldguardRegionCache = manager.getRegion(worldguardRegion);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public int getXRange() {
        return xrange;
    }

    public int getYRange() {
        return yrange;
    }

    public boolean isPathfind() {
        return pathfind;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void load(DataKey key) {
        recalculateTree();
    }

    @Override
    public void onRemove() {
        worldguardRegionCache = null;
        if (currentGoal == null)
            return;
        currentGoal.pause();
        npc.getDefaultGoalController().removeGoal(currentGoal);
        currentGoal = null;
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = WanderGoal.builder(npc).xrange(xrange).yrange(yrange).pathfind(pathfind)
                    .tree(() -> regionCentres.isEmpty() ? null : tree).delay(delay)
                    .worldguardRegion(this::getWorldGuardRegion).build();
            if (paused) {
                currentGoal.pause();
            }
        }
        Iterator<GoalEntry> itr = npc.getDefaultGoalController().iterator();
        while (itr.hasNext()) {
            if (itr.next() instanceof WanderGoal) {
                itr.remove();
            }
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);
    }

    private void recalculateTree() {
        tree = PhTreeSolid.create(3);
        for (Location loc : regionCentres) {
            long[] lower = { loc.getBlockX() - xrange, loc.getBlockY() - yrange, loc.getBlockZ() - xrange };
            long[] upper = { loc.getBlockX() + xrange, loc.getBlockY() + yrange, loc.getBlockZ() + xrange };
            tree.put(lower, upper, true);
        }
    }

    public void removeRegionCentre(Location centre) {
        regionCentres.remove(centre);
        recalculateTree();
    }

    public void removeRegionCentres(Collection<Location> centre) {
        regionCentres.removeAll(centre);
        recalculateTree();
    }

    @Override
    public void save(DataKey key) {
    }

    public void setDelay(int delay) {
        this.delay = delay;
        if (currentGoal != null) {
            currentGoal.setDelay(delay);
        }
    }

    public void setPathfind(boolean pathfind) {
        this.pathfind = pathfind;
        if (currentGoal != null) {
            currentGoal.setPathfind(pathfind);
        }
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (currentGoal != null) {
            if (paused) {
                currentGoal.pause();
            } else {
                currentGoal.unpause();
            }
        }
    }

    public void setWorldGuardRegion(String region) {
        worldguardRegion = region;
        worldguardRegionCache = null;
    }

    public void setXYRange(int xrange, int yrange) {
        this.xrange = xrange;
        this.yrange = yrange;
        if (currentGoal != null) {
            currentGoal.setXYRange(xrange, yrange);
        }
    }

    private class RecalculateList extends ForwardingList<Location> {
        @Override
        public void add(int idx, Location loc) {
            super.add(idx, loc);
            recalculateTree();
        }

        @Override
        public boolean add(Location loc) {
            boolean val = super.add(loc);
            recalculateTree();
            return val;
        }

        @Override
        protected List<Location> delegate() {
            return regionCentres;
        }

        @Override
        public Location remove(int idx) {
            Location val = super.remove(idx);
            recalculateTree();
            return val;
        }

        @Override
        public Location set(int idx, Location idx2) {
            Location val = super.set(idx, idx2);
            recalculateTree();
            return val;
        }
    }

    private static int DEFAULT_XRANGE = 25;
    private static int DEFAULT_YRANGE = 3;
}
