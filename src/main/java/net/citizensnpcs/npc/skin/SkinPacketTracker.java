package net.citizensnpcs.npc.skin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.citizensnpcs.npc.CitizensNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Preconditions;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;

/**
 * Handles and synchronizes add and remove packets for Player type NPC's in order to properly apply the NPC skin.
 *
 * <p>
 * Used as one instance per NPC entity.
 * </p>
 */
public class SkinPacketTracker {
    private final SkinnableEntity entity;
    private final Map<UUID, PlayerEntry> inProgress = new HashMap<UUID, PlayerEntry>(Bukkit.getMaxPlayers() / 2);

    private boolean isRemoved;
    private Skin skin;

    /**
     * Constructor.
     *
     * @param entity
     *            The skinnable entity the instance belongs to.
     */
    public SkinPacketTracker(SkinnableEntity entity) {
        Preconditions.checkNotNull(entity);

        this.entity = entity;
        this.skin = Skin.get(entity);

        if (LISTENER == null) {
            LISTENER = new PlayerListener();
            Bukkit.getPluginManager().registerEvents(LISTENER, CitizensAPI.getPlugin());
        }
    }

    /**
     * Get the NPC skin.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Notify the tracker that a remove packet has been sent to the specified player.
     *
     * @param playerId
     *            The ID of the player.
     */
    void notifyRemovePacketCancelled(UUID playerId) {
        inProgress.remove(playerId);
    }

    /**
     * Notify the tracker that a remove packet has been sent to the specified player.
     *
     * @param playerId
     *            The ID of the player.
     */
    void notifyRemovePacketSent(UUID playerId) {
        PlayerEntry entry = inProgress.get(playerId);
        if (entry == null)
            return;

        if (entry.removeCount == 0)
            return;

        entry.removeCount -= 1;
        if (entry.removeCount == 0) {
            inProgress.remove(playerId);
        } else {
            scheduleRemovePacket(entry);
        }
    }

    /**
     * Notify that the NPC skin has been changed.
     */
    public void notifySkinChange() {
        this.skin = Skin.get(entity);
        skin.applyAndRespawn(entity);
    }

    /**
     * Invoke when the NPC entity is spawned.
     */
    public void onSpawnNPC() {
        isRemoved = false;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.getNPC().isSpawned())
                    return;

                double viewDistance = Settings.Setting.NPC_SKIN_VIEW_DISTANCE.asDouble();
                updateNearbyViewers(viewDistance);
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 20);
    }

    /**
     * Invoke when the NPC entity is removed.
     *
     * <p>
     * Sends remove packets to all players.
     * </p>
     */
    public void onRemoveNPC() {
        isRemoved = true;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for (Player player : players) {

            if (player.hasMetadata("NPC"))
                continue;

            // send packet now and later to ensure removal from player list
            NMS.sendPlayerListRemove(player, entity.getBukkitEntity());
            PLAYER_LIST_REMOVER.sendPacket(player, entity);
        }
    }

    private void scheduleRemovePacket(final PlayerEntry entry) {
        if (isRemoved)
            return;

        entry.removeTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (shouldRemoveFromPlayerList()) {
                    PLAYER_LIST_REMOVER.sendPacket(entry.player, entity);
                }
            }
        }, PACKET_DELAY_REMOVE);
    }

    private void scheduleRemovePacket(PlayerEntry entry, int count) {
        if (!shouldRemoveFromPlayerList())
            return;

        entry.removeCount = count;
        scheduleRemovePacket(entry);
    }

    private boolean shouldRemoveFromPlayerList() {
        boolean isTablistDisabled = Settings.Setting.DISABLE_TABLIST.asBoolean();
        boolean isNpcRemoved = entity.getNPC().data().get("removefromplayerlist",
                Settings.Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());

        return isNpcRemoved && isTablistDisabled;
    }

    /**
     * Send skin related packets to all nearby players within the specified block radius.
     *
     * @param radius
     *            The radius.
     */
    public void updateNearbyViewers(double radius) {
        radius *= radius;

        org.bukkit.World world = entity.getBukkitEntity().getWorld();
        Player from = entity.getBukkitEntity();
        Location location = from.getLocation();

        for (Player player : world.getPlayers()) {
            if (player == null || player.hasMetadata("NPC"))
                continue;

            if (!player.canSee(from))
                continue;

            if (location.distanceSquared(player.getLocation(CACHE_LOCATION)) > radius)
                continue;

            updateViewer(player);
        }
    }

    /**
     * Send skin related packets to a player.
     *
     * @param player
     *            The player.
     */
    public void updateViewer(final Player player) {
        Preconditions.checkNotNull(player);

        if (isRemoved || player.hasMetadata("NPC"))
            return;

        PlayerEntry entry = inProgress.get(player.getUniqueId());
        if (entry != null) {
            entry.cancel();
        } else {
            entry = new PlayerEntry(player);
        }

        PLAYER_LIST_REMOVER.cancelPackets(player, entity);

        inProgress.put(player.getUniqueId(), entry);
        skin.apply(entity);
        NMS.sendPlayerListAdd(player, entity.getBukkitEntity());

        scheduleRemovePacket(entry, 2);
    }

    private class PlayerEntry {
        Player player;
        int removeCount;
        BukkitTask removeTask;

        PlayerEntry(Player player) {
            this.player = player;
        }

        // cancel previous packet tasks so they do not interfere with
        // new tasks
        void cancel() {
            if (removeTask != null)
                removeTask.cancel();
            removeCount = 0;
        }
    }

    private static class PlayerListener implements Listener {
        @EventHandler
        private void onPlayerQuit(PlayerQuitEvent event) {
            // this also causes any entries in the "inProgress" field to
            // be removed.
            PLAYER_LIST_REMOVER.cancelPackets(event.getPlayer());
        }
    }

    private static final Location CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static PlayerListener LISTENER;
    private static final int PACKET_DELAY_REMOVE = 1;
    private static final PlayerListRemover PLAYER_LIST_REMOVER = new PlayerListRemover();
}
