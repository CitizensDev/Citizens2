package net.citizensnpcs.npc.entity;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.skin.NPCSkin;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class EntityHumanPacketTracker {

    private final Map<UUID, Entry> entries = new HashMap<UUID, Entry>(Bukkit.getMaxPlayers());
    private final EntityHumanNPC entity;

    // tracks sent and scheduled packets
    public EntityHumanPacketTracker(EntityHumanNPC entity) {

        this.entity = entity;

        if (LISTENER == null) {
            LISTENER = new PlayerListener();
            Bukkit.getPluginManager().registerEvents(LISTENER, CitizensAPI.getPlugin());
        }

        TRACKERS.put(this, null);
    }

    // send PacketPlayOutPlayerInfo (PACKET_ADD) packet to a player
    public void addViewer(final EntityPlayer entityPlayer) {

        CraftPlayer player = entityPlayer.getBukkitEntity();

        Entry entry = entries.get(player.getUniqueId());
        if (entry != null)
            return;

        entry = new Entry(player.getUniqueId());
        entries.put(player.getUniqueId(), entry);

        NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));

        final Entry finalEntry = entry;
        entry.skinTask = Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                sendSkin(entityPlayer, finalEntry);
            }
        }, 1);
    }

    // send PacketPlayOutPlayerInfo (PLAYER_ADD) to all players within a radius
    public void addNearbyViewers(double radius) {

        radius *= radius;

        org.bukkit.World world = entity.getBukkitEntity().getWorld();
        CraftPlayer from = entity.getBukkitEntity();
        Location location = from.getLocation();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            if (player == null || world != player.getWorld() || !player.canSee(from))
                continue;

            if (location.distanceSquared(player.getLocation(CACHE_LOCATION)) > radius)
                continue;

            addViewer(((CraftPlayer) player).getHandle());
        }
    }

    // Send PacketPlayOutPlayerInfo (PLAYER_REMOVE) to all players.
    public void sendRemovePacket() {

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for (Player player : players) {

            Entry entry = entries.get(player.getUniqueId());
            if (entry != null)
                entry.cancelTasks();

            sendRemovePacket(((CraftPlayer) player).getHandle(), true);
        }
    }

    // send skin packets
    private void sendSkin(final EntityPlayer entityPlayer, final Entry entry) {

        WorldServer nmsWorld = (WorldServer) entity.world;
        new NPCSkin(entity.getNPC()).setSkin(nmsWorld, entity.getProfile(),
                new Runnable() {
                    @Override
                    public void run() {
                        scheduleRemovePacket(entityPlayer, entry);
                    }
                });
    }

    // Send PacketPlayOutPlayerInfo (PLAYER_REMOVE) to a player.
    private void sendRemovePacket(EntityPlayer entityPlayer, boolean forceRemove) {

        CraftPlayer player = entityPlayer.getBukkitEntity();

        if (forceRemove || Settings.Setting.DISABLE_TABLIST.asBoolean() ||
                entity.getNPC().data().get("removefromplayerlist",
                        Settings.Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean())) {

            NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));
        }

        entries.remove(player.getUniqueId());
    }

    // Schedule a PacketPlayOutPlayerInfo (PLAYER_REMOVE) to be sent to a player.
    private void scheduleRemovePacket(final EntityPlayer entityPlayer, final Entry entry) {

        entry.scheduledRemoval = Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                new Runnable() {
                    @Override
                    public void run() {
                        sendRemovePacket(entityPlayer, false);
                    }
                }, 1);
    }

    private class Entry {
        UUID playerId;
        BukkitTask skinTask;
        int scheduledRemoval;

        Entry(UUID playerId) {
            this.playerId = playerId;
        }

        void cancelTasks() {

            if (skinTask != null)
                skinTask.cancel();

            Bukkit.getScheduler().cancelTask(scheduledRemoval);
        }
    }

    private static class PlayerListener implements Listener {

        @EventHandler
        private void onPlayerQuit(PlayerQuitEvent event) {

            // remove players that log out from tracker instances
            for (EntityHumanPacketTracker tracker : TRACKERS.keySet()) {
                Entry entry = tracker.entries.remove(event.getPlayer().getUniqueId());
                if (entry != null)
                    entry.cancelTasks();
            }
        }
    }

    private static final Location CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final Map<EntityHumanPacketTracker, Void> TRACKERS =
            new WeakHashMap<EntityHumanPacketTracker, Void>(Bukkit.getMaxPlayers() / 2);

    private static PlayerListener LISTENER;
}
