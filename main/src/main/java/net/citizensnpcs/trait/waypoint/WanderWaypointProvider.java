package net.citizensnpcs.trait.waypoint;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;

import ch.ethz.globis.phtree.PhTreeSolid;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController.GoalEntry;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
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
public class WanderWaypointProvider
        implements WaypointProvider, Supplier<PhTreeSolid<Boolean>>, Function<NPC, Location> {
    private WanderGoal currentGoal;
    @Persist
    public int delay = -1;
    private NPC npc;
    private volatile boolean paused;
    @Persist
    private final List<Location> regionCentres = Lists.newArrayList();
    private PhTreeSolid<Boolean> tree = PhTreeSolid.create(3);
    @Persist
    public int xrange = DEFAULT_XRANGE;
    @Persist
    public int yrange = DEFAULT_YRANGE;

    public void addRegionCentre(Location centre) {
        regionCentres.add(centre);
        recalculateTree();
    }

    public void addRegionCentres(Collection<Location> centre) {
        regionCentres.addAll(centre);
        recalculateTree();
    }

    @Override
    public Location apply(NPC npc) {
        Location closestCentre = null;
        double minDist = Double.MAX_VALUE;
        for (Location centre : regionCentres) {
            double d = centre.distanceSquared(npc.getStoredLocation());
            if (d < minDist) {
                minDist = d;
                closestCentre = centre;
            }
        }
        if (closestCentre != null) {
            Location randomLocation = MinecraftBlockExaminer.findRandomValidLocation(npc.getEntity().getLocation(),
                    xrange, yrange, new Function<Block, Boolean>() {
                        @Override
                        public Boolean apply(Block block) {
                            if ((block.getRelative(BlockFace.UP).isLiquid() || block.getRelative(0, 2, 0).isLiquid())
                                    && npc.getNavigator().getDefaultParameters().avoidWater()) {
                                return false;
                            }
                            return true;
                        }
                    }, Util.getFastRandom());
            if (randomLocation != null) {
                return randomLocation;
            }
            // TODO: should find closest edge block that is valid
            return MinecraftBlockExaminer.findValidLocation(closestCentre, xrange, yrange);
        }
        return null;
    }

    @Override
    public WaypointEditor createEditor(final CommandSender sender, CommandContext args) {
        return new WaypointEditor() {
            boolean editingRegions = false;
            EntityMarkers<Location> markers = new EntityMarkers<Location>();

            @Override
            public void begin() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_BEGIN);
                if (currentGoal != null) {
                    currentGoal.pause();
                }
            }

            @Override
            public void end() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_END);
                editingRegions = false;
                if (currentGoal != null) {
                    currentGoal.unpause();
                }
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
                            xrange = range;
                        } else {
                            yrange = range;
                        }
                        if (currentGoal != null) {
                            currentGoal.setXYRange(xrange, yrange);
                        }
                        recalculateTree();
                    } catch (Exception ex) {
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_RANGE_SET, xrange, yrange);
                        }
                    });
                } else if (message.startsWith("regions")) {
                    event.setCancelled(true);
                    editingRegions = !editingRegions;
                    if (editingRegions) {
                        for (Location regionCentre : regionCentres) {
                            Entity entity = markers.createMarker(regionCentre, regionCentre);
                            entity.setMetadata("wandermarker",
                                    new FixedMetadataValue(CitizensAPI.getPlugin(), regionCentre));
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_REGION_EDITING_START);
                            }
                        });
                    } else {
                        markers.destroyMarkers();
                    }
                } else if (message.startsWith("delay")) {
                    event.setCancelled(true);
                    try {
                        delay = Integer.parseInt(message.split(" ")[1]);
                        if (currentGoal != null) {
                            currentGoal.setDelay(delay);
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {

                            @Override
                            public void run() {
                                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_DELAY_SET, delay);
                            }
                        });
                    } catch (

                    Exception e) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                Messaging.sendErrorTr(sender, Messages.WANDER_WAYPOINTS_INVALID_DELAY);
                            }
                        });
                    }
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
                if (!sender.equals(event.getPlayer()) || !editingRegions || Util.isOffHand(event))
                    return;
                if (!event.getRightClicked().hasMetadata("wandermarker"))
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

    @Override
    public PhTreeSolid<Boolean> get() {
        return regionCentres.isEmpty() ? null : tree;
    }

    public List<Location> getRegionCentres() {
        return new RecalculateList();
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
        npc.getDefaultGoalController().removeGoal(currentGoal);
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = WanderGoal.createWithNPCAndRangeAndTreeAndFallback(npc, xrange, yrange,
                    WanderWaypointProvider.this, WanderWaypointProvider.this);
            currentGoal.setDelay(delay);
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
        tree.clear();
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

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
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

    private static final int DEFAULT_XRANGE = 25;
    private static final int DEFAULT_YRANGE = 3;
}
