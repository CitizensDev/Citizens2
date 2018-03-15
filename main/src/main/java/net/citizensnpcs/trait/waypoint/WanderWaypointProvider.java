package net.citizensnpcs.trait.waypoint;

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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.cuboid.QuadCuboid;
import net.citizensnpcs.api.util.cuboid.QuadTree;
import net.citizensnpcs.util.Messages;

public class WanderWaypointProvider implements WaypointProvider, Supplier<QuadTree> {
    private WanderGoal currentGoal;
    private NPC npc;
    private volatile boolean paused;
    @Persist
    private final List<Location> regionCentres = Lists.newArrayList();
    private QuadTree tree = new QuadTree();
    @Persist
    public int xrange = DEFAULT_XRANGE;
    @Persist
    public int yrange = DEFAULT_YRANGE;

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
                }
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(sender) || event.getAction() == Action.PHYSICAL || !npc.isSpawned()
                        || event.getPlayer().getWorld() != npc.getEntity().getWorld()
                        || event.getHand() == EquipmentSlot.OFF_HAND)
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
                if (!sender.equals(event.getPlayer()) || !editingRegions || event.getHand() == EquipmentSlot.OFF_HAND)
                    return;
                if (!event.getRightClicked().hasMetadata("wandermarker"))
                    return;
                regionCentres.remove(event.getRightClicked().getMetadata("wandermarker").get(0).value());
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_REMOVED_REGION,
                        formatLoc((Location) event.getRightClicked().getMetadata("wandermarker").get(0).value()),
                        regionCentres.size());
                recalculateTree();
            }
        };
    }

    @Override
    public QuadTree get() {
        return regionCentres.isEmpty() ? null : tree;
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
            currentGoal = WanderGoal.createWithNPCAndRangeAndTree(npc, xrange, yrange, WanderWaypointProvider.this);
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);
    }

    private void recalculateTree() {
        tree = new QuadTree();
        for (Location loc : regionCentres) {
            tree.insert(new QuadCuboid(loc.getBlockX() - xrange, loc.getBlockY() - yrange, loc.getBlockZ() - xrange,
                    loc.getBlockX() + xrange, loc.getBlockY() + yrange, loc.getBlockZ() + xrange));
        }
    }

    @Override
    public void save(DataKey key) {
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private static final int DEFAULT_XRANGE = 3;
    private static final int DEFAULT_YRANGE = 25;
}
