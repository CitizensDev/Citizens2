package net.citizensnpcs.npc.skin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.util.Util;

/**
 * Tracks skin updates for players.
 *
 * @see net.citizensnpcs.EventListen
 */
public class SkinUpdateTracker {

    private final Map<SkinnableEntity, Void> navigating = new WeakHashMap<SkinnableEntity, Void>(25);
    private final NPCRegistry npcRegistry;
    private final Map<UUID, PlayerTracker> playerTrackers = new HashMap<UUID, PlayerTracker>(
            Bukkit.getMaxPlayers() / 2);
    private final Map<String, NPCRegistry> registries;
    private final NPCNavigationUpdater updater = new NPCNavigationUpdater();

    /**
     * Constructor.
     *
     * @param npcRegistry
     *            The primary citizens registry.
     * @param registries
     *            Map of other registries.
     */
    public SkinUpdateTracker(NPCRegistry npcRegistry, Map<String, NPCRegistry> registries) {
        Preconditions.checkNotNull(npcRegistry);
        Preconditions.checkNotNull(registries);

        this.npcRegistry = npcRegistry;
        this.registries = registries;

        updater.runTaskTimer(CitizensAPI.getPlugin(), 1, 1);
        new NPCNavigationTracker().runTaskTimer(CitizensAPI.getPlugin(), 3, 7);
    }

    // determines if a player is near a skinnable entity and, if checkFov set, if the
    // skinnable entity is within the players field of view.
    private boolean canSee(Player player, SkinnableEntity skinnable, boolean checkFov) {
        Player entity = skinnable.getBukkitEntity();
        if (entity == null)
            return false;

        if (!player.canSee(entity))
            return false;

        if (!player.getWorld().equals(entity.getWorld()))
            return false;

        Location playerLoc = player.getLocation(CACHE_LOCATION);
        Location skinLoc = entity.getLocation(NPC_LOCATION);

        double viewDistance = Settings.Setting.NPC_SKIN_VIEW_DISTANCE.asDouble();
        viewDistance *= viewDistance;

        if (playerLoc.distanceSquared(skinLoc) > viewDistance)
            return false;

        // see if the NPC is within the players field of view
        if (checkFov) {
            double deltaX = skinLoc.getX() - playerLoc.getX();
            double deltaZ = skinLoc.getZ() - playerLoc.getZ();
            double angle = Math.atan2(deltaX, deltaZ);
            float skinYaw = Util.clampYaw(-(float) Math.toDegrees(angle));
            float playerYaw = Util.clampYaw(playerLoc.getYaw());
            float upperBound = Util.clampYaw(playerYaw + FIELD_OF_VIEW);
            float lowerBound = Util.clampYaw(playerYaw - FIELD_OF_VIEW);
            if (upperBound == -180.0 && playerYaw > 0) {
                upperBound = 0;
            }
            boolean hasMoved;
            if (playerYaw - 90 < -180 || playerYaw + 90 > 180) {
                hasMoved = skinYaw > lowerBound && skinYaw < upperBound;
            } else {
                hasMoved = skinYaw < lowerBound || skinYaw > upperBound;
            }
            return hasMoved;
        }

        return true;
    }

    private Iterable<NPC> getAllNPCs() {
        return Iterables.filter(Iterables.concat(npcRegistry, Iterables.concat(registries.values())),
                Predicates.notNull());
    }

    private List<SkinnableEntity> getNearbyNPCs(Player player, boolean reset, boolean checkFov) {
        List<SkinnableEntity> results = new ArrayList<SkinnableEntity>();
        PlayerTracker tracker = getTracker(player, reset);
        for (NPC npc : getAllNPCs()) {

            SkinnableEntity skinnable = getSkinnable(npc);
            if (skinnable == null)
                continue;

            // if checking field of view, don't add skins that have already been updated for FOV
            if (checkFov && tracker.fovVisibleSkins.contains(skinnable))
                continue;

            if (canSee(player, skinnable, checkFov)) {
                results.add(skinnable);
            }
        }
        return results;
    }

    // get all navigating skinnable NPC's within the players FOV that have not been "seen" yet
    private void getNewVisibleNavigating(Player player, Collection<SkinnableEntity> output) {
        PlayerTracker tracker = getTracker(player, false);

        for (SkinnableEntity skinnable : navigating.keySet()) {

            // make sure player hasn't already been updated to prevent excessive tab list flashing
            // while NPC's are navigating and to reduce the number of times #canSee is invoked.
            if (tracker.fovVisibleSkins.contains(skinnable))
                continue;

            if (canSee(player, skinnable, true))
                output.add(skinnable);
        }
    }

    @Nullable
    private SkinnableEntity getSkinnable(NPC npc) {
        Entity entity = npc.getEntity();
        if (entity == null)
            return null;

        return entity instanceof SkinnableEntity ? (SkinnableEntity) entity : null;
    }

    // get a players tracker, create new one if not exists.
    private PlayerTracker getTracker(Player player, boolean reset) {
        PlayerTracker tracker = playerTrackers.get(player.getUniqueId());
        if (tracker == null) {
            tracker = new PlayerTracker(player);
            playerTrackers.put(player.getUniqueId(), tracker);
        } else if (reset) {
            tracker.hardReset(player);
        }
        return tracker;
    }

    /**
     * Invoke when an NPC is despawned.
     *
     * @param npc
     *            The despawned NPC.
     */
    public void onNPCDespawn(NPC npc) {
        Preconditions.checkNotNull(npc);
        SkinnableEntity skinnable = getSkinnable(npc);
        if (skinnable == null)
            return;

        navigating.remove(skinnable);

        for (PlayerTracker tracker : playerTrackers.values()) {
            tracker.fovVisibleSkins.remove(skinnable);
        }
    }

    /**
     * Invoke when an NPC begins navigating.
     *
     * @param npc
     *            The navigating NPC.
     */
    public void onNPCNavigationBegin(NPC npc) {
        Preconditions.checkNotNull(npc);
        SkinnableEntity skinnable = getSkinnable(npc);
        if (skinnable == null)
            return;

        navigating.put(skinnable, null);
    }

    /**
     * Invoke when an NPC finishes navigating.
     *
     * @param npc
     *            The finished NPC.
     */
    public void onNPCNavigationComplete(NPC npc) {
        Preconditions.checkNotNull(npc);
        SkinnableEntity skinnable = getSkinnable(npc);
        if (skinnable == null)
            return;

        navigating.remove(skinnable);
    }

    /**
     * Invoke when an NPC is spawned.
     *
     * @param npc
     *            The spawned NPC.
     */
    public void onNPCSpawn(NPC npc) {
        Preconditions.checkNotNull(npc);
        SkinnableEntity skinnable = getSkinnable(npc);
        if (skinnable == null)
            return;

        // reset nearby players in case they are not looking at the NPC when it spawns.
        resetNearbyPlayers(skinnable);
    }

    /**
     * Invoke when a player moves.
     *
     * @param player
     *            The player that moved.
     */
    public void onPlayerMove(Player player) {
        Preconditions.checkNotNull(player);
        PlayerTracker updateTracker = playerTrackers.get(player.getUniqueId());
        if (updateTracker == null)
            return;

        if (!updateTracker.shouldUpdate(player))
            return;

        updatePlayer(player, 10, false);
    }

    /**
     * Remove a player from the tracker.
     *
     * <p>
     * Used when the player logs out.
     * </p>
     *
     * @param playerId
     *            The ID of the player.
     */
    public void removePlayer(UUID playerId) {
        Preconditions.checkNotNull(playerId);
        playerTrackers.remove(playerId);
    }

    /**
     * Reset all players currently being tracked.
     *
     * <p>
     * Used when Citizens is reloaded.
     * </p>
     */
    public void reset() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC"))
                continue;

            PlayerTracker tracker = playerTrackers.get(player.getUniqueId());
            if (tracker == null)
                continue;

            tracker.hardReset(player);
        }
    }

    // hard reset players near a skinnable NPC
    private void resetNearbyPlayers(SkinnableEntity skinnable) {
        Entity entity = skinnable.getBukkitEntity();
        if (entity == null || !entity.isValid())
            return;

        double viewDistance = Settings.Setting.NPC_SKIN_VIEW_DISTANCE.asDouble();
        viewDistance *= viewDistance;
        Location location = entity.getLocation(NPC_LOCATION);
        List<Player> players = entity.getWorld().getPlayers();
        for (Player player : players) {
            if (player.hasMetadata("NPC"))
                continue;
            Location ploc = player.getLocation(CACHE_LOCATION);
            if (ploc.getWorld() != location.getWorld())
                continue;
            double distanceSquared = ploc.distanceSquared(location);
            if (distanceSquared > viewDistance)
                continue;

            PlayerTracker tracker = playerTrackers.get(player.getUniqueId());
            if (tracker != null) {
                tracker.hardReset(player);
            }
        }
    }

    /**
     * Update a player with skin related packets from nearby skinnable NPC's.
     *
     * @param player
     *            The player to update.
     * @param delay
     *            The delay before sending the packets.
     * @param reset
     *            True to hard reset the players tracking info, otherwise false.
     */
    public void updatePlayer(final Player player, long delay, final boolean reset) {
        if (player.hasMetadata("NPC"))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                List<SkinnableEntity> visible = getNearbyNPCs(player, reset, false);
                for (SkinnableEntity skinnable : visible) {
                    skinnable.getSkinTracker().updateViewer(player);
                }
            }
        }.runTaskLater(CitizensAPI.getPlugin(), delay);
    }

    // update players when the NPC navigates into their field of view
    private class NPCNavigationTracker extends BukkitRunnable {
        @Override
        public void run() {
            if (navigating.isEmpty() || playerTrackers.isEmpty())
                return;

            List<SkinnableEntity> nearby = new ArrayList<SkinnableEntity>(10);
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (Player player : players) {
                if (player.hasMetadata("NPC"))
                    continue;

                getNewVisibleNavigating(player, nearby);

                for (SkinnableEntity skinnable : nearby) {
                    PlayerTracker tracker = getTracker(player, false);
                    tracker.fovVisibleSkins.add(skinnable);
                    updater.queue.offer(new UpdateInfo(player, skinnable));
                }

                nearby.clear();
            }
        }
    }

    // Updates players. Repeating task used to schedule updates without
    // causing excessive scheduling.
    private class NPCNavigationUpdater extends BukkitRunnable {
        Queue<UpdateInfo> queue = new ArrayDeque<UpdateInfo>(20);

        @Override
        public void run() {
            while (!queue.isEmpty()) {
                UpdateInfo info = queue.remove();
                info.entity.getSkinTracker().updateViewer(info.player);
            }
        }
    }

    // Tracks player location and yaw to determine when the player should be updated
    // with nearby skins.
    private class PlayerTracker {
        final Set<SkinnableEntity> fovVisibleSkins = new HashSet<SkinnableEntity>(20);
        boolean hasMoved;
        final Location location = new Location(null, 0, 0, 0);
        float lowerBound;
        int rotationCount;
        float startYaw;
        float upperBound;

        PlayerTracker(Player player) {
            hardReset(player);
        }

        // reset all
        void hardReset(Player player) {
            this.hasMoved = false;
            this.rotationCount = 0;
            this.lowerBound = this.upperBound = this.startYaw = 0;
            this.fovVisibleSkins.clear();
            reset(player);
        }

        // resets initial yaw and location to the players current location and yaw.
        void reset(Player player) {
            player.getLocation(this.location);
            if (rotationCount < 3) {
                float rotationDegrees = Settings.Setting.NPC_SKIN_ROTATION_UPDATE_DEGREES.asFloat();
                float yaw = Util.clampYaw(this.location.getYaw());
                this.startYaw = yaw;
                this.upperBound = Util.clampYaw(yaw + rotationDegrees);
                this.lowerBound = Util.clampYaw(yaw - rotationDegrees);
                if (upperBound == -180.0 && startYaw > 0) {
                    upperBound = 0;
                }
            }
        }

        boolean shouldUpdate(Player player) {
            Location currentLoc = player.getLocation(CACHE_LOCATION);

            if (!hasMoved) {
                hasMoved = true;
                return true;
            }

            if (rotationCount < 3) {
                float yaw = Util.clampYaw(currentLoc.getYaw());
                boolean hasRotated;
                if (startYaw - 90 < -180 || startYaw + 90 > 180) {
                    hasRotated = yaw > lowerBound && yaw < upperBound;
                } else {
                    hasRotated = yaw < lowerBound || yaw > upperBound;
                }

                // update the first 3 times the player rotates. helps load skins around player
                // after the player logs/teleports.
                if (hasRotated) {
                    rotationCount++;
                    reset(player);
                    return true;
                }
            }

            // make sure player is in same world
            if (!currentLoc.getWorld().equals(this.location.getWorld())) {
                reset(player);
                return true;
            }

            // update every time a player moves a certain distance
            double distance = currentLoc.distanceSquared(this.location);
            if (distance > MOVEMENT_SKIN_UPDATE_DISTANCE) {
                reset(player);
                return true;
            } else {
                return false;
            }
        }
    }

    private static class UpdateInfo {
        SkinnableEntity entity;
        Player player;

        UpdateInfo(Player player, SkinnableEntity entity) {
            this.player = player;
            this.entity = entity;
        }
    }

    private static final Location CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final float FIELD_OF_VIEW = 70f;
    private static final int MOVEMENT_SKIN_UPDATE_DISTANCE = 50 * 50;
    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}
