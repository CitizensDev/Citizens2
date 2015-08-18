package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
    public void sendAddPacket(EntityPlayer entityPlayer) {

        CraftPlayer player = entityPlayer.getBukkitEntity();

        Entry entry = getEntry(player.getUniqueId());

        Bukkit.getScheduler().cancelTask(entry.scheduledRemoval);

        NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));

        entry.isAdded = true;
    }

    // send PacketPlayOutPlayerInfo (PLAYER_ADD) to all players within a radius
    public void sendAddPacketNearby(double radius) {

        radius *= radius;

        org.bukkit.World world = entity.getBukkitEntity().getWorld();
        CraftPlayer from = entity.getBukkitEntity();
        Location location = from.getLocation();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            if (player == null || world != player.getWorld() || !player.canSee(from))
                continue;

            if (location.distanceSquared(player.getLocation(CACHE_LOCATION)) > radius)
                continue;

            Entry entry = getEntry(player.getUniqueId());

            Bukkit.getScheduler().cancelTask(entry.scheduledRemoval);

            NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));

            entry.isAdded = true;
        }
    }

    // Send PacketPlayOutPlayerInfo (PLAYER_REMOVE) to a player.
    // Does not send packet unless the player has already received PLAYER_ADD
    public void sendRemovePacket(EntityPlayer entityPlayer) {

        CraftPlayer player = entityPlayer.getBukkitEntity();

        Entry entry = getEntry(player.getUniqueId());
        if (!entry.isAdded)
            return;

        Bukkit.getScheduler().cancelTask(entry.scheduledRemoval);

        NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));

        entry.isAdded = false;
    }

    // Send PacketPlayOutPlayerInfo (PLAYER_REMOVE) to all players.
    public void sendRemovePacket() {

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for (Player player : players) {

            Entry entry = getEntry(player.getUniqueId());

            Bukkit.getScheduler().cancelTask(entry.scheduledRemoval);

            NMS.sendPacket(player, new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));

            entry.isAdded = false;
        }
    }

    // Schedule a PacketPlayOutPlayerInfo (PLAYER_REMOVE) to be sent to a player.
    public void scheduleRemovePacket(final EntityPlayer entityPlayer) {

        Player player = entityPlayer.getBukkitEntity();
        Entry entry = getEntry(player.getUniqueId());

        Bukkit.getScheduler().cancelTask(entry.scheduledRemoval);

        entry.scheduledRemoval = Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                new Runnable() {
                    @Override
                    public void run() {
                        sendRemovePacket(entityPlayer);
                    }
                }, 10);
    }

    private Entry getEntry(UUID playerId) {
        Entry entry = entries.get(playerId);
        if (entry == null) {
            entry = new Entry(playerId);
            entries.put(playerId, entry);
        }
        return entry;
    }

    private class Entry {
        UUID playerId;
        boolean isAdded; // indicates the Player Add packet has been sent and remove packet not sent.
        int scheduledRemoval;

        Entry(UUID playerId) {
            this.playerId = playerId;
        }
    }

    private static class PlayerListener implements Listener {

        @EventHandler
        private void onPlayerQuit(PlayerQuitEvent event) {

            // remove players that log out from tracker instances
            for (EntityHumanPacketTracker tracker : TRACKERS.keySet()) {
                tracker.entries.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    private static Location CACHE_LOCATION = new Location(null, 0, 0, 0);
    private static final Map<EntityHumanPacketTracker, Void> TRACKERS =
            new WeakHashMap<EntityHumanPacketTracker, Void>(Bukkit.getMaxPlayers());

    private static PlayerListener LISTENER;
}
