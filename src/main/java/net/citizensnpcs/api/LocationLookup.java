package net.citizensnpcs.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import ch.ethz.globis.phtree.PhTreeF;

public class LocationLookup implements Runnable {
    private final Map<String, PerPlayerMetadata> metadata = Maps.newHashMap();
    private final Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public PerPlayerMetadata getMetadata(String key) {
        return metadata.get(key);
    }

    public Iterable<Player> getNearbyPlayers(Location base, double dist) {
        PhTreeF<Player> tree = worlds.get(base.getWorld().getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.rangeQuery(dist, base.getX(), base.getY(), base.getZ());
    }

    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            updateWorld(event.getPlayer().getWorld());
            for (PerPlayerMetadata meta : metadata.values()) {
                if (meta.onJoin != null) {
                    meta.onJoin.accept(meta, event);
                }
            }
        });
    }

    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            updateWorld(event.getPlayer().getWorld());
            for (PerPlayerMetadata meta : metadata.values()) {
                meta.sent.removeAll(event.getPlayer().getUniqueId());
            }
        });
    }

    public PerPlayerMetadata registerMetadata(String key, BiConsumer<PerPlayerMetadata, PlayerJoinEvent> onJoin) {
        return metadata.computeIfAbsent(key, (s) -> new PerPlayerMetadata(onJoin));
    }

    @Override
    public void run() {
        Bukkit.getServer().getWorlds().forEach(this::updateWorld);
    }

    private void updateWorld(World world) {
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            worlds.remove(world.getUID());
            return;
        }
        PhTreeF<Player> tree = worlds.computeIfAbsent(world.getUID(), uid -> PhTreeF.create(3));
        tree.clear();
        Location loc = new Location(null, 0, 0, 0);
        for (Player player : players) {
            if (player.hasMetadata("NPC"))
                continue;
            player.getLocation(loc);
            tree.put(new double[] { loc.getX(), loc.getY(), loc.getZ() }, player);
        }
    }

    public static class PerPlayerMetadata {
        private final BiConsumer<PerPlayerMetadata, PlayerJoinEvent> onJoin;
        public SetMultimap<UUID, String> sent = HashMultimap.create();

        public PerPlayerMetadata(BiConsumer<PerPlayerMetadata, PlayerJoinEvent> onJoin) {
            this.onJoin = onJoin;
        }
    }
}
