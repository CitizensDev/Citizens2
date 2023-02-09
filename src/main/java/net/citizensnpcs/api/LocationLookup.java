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
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

import ch.ethz.globis.phtree.PhTreeF;

public class LocationLookup extends BukkitRunnable {
    private final Map<String, PerPlayerMetadata<?>> metadata = Maps.newHashMap();
    private final Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public PerPlayerMetadata<?> getMetadata(String key) {
        return metadata.get(key);
    }

    public Iterable<Player> getNearbyPlayers(Location base, double dist) {
        PhTreeF<Player> tree = worlds.get(base.getWorld().getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.rangeQuery(dist, base.getX(), base.getY(), base.getZ());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            for (PerPlayerMetadata<?> meta : metadata.values()) {
                meta.sent.remove(event.getPlayer().getUniqueId());
            }
        });
    }

    public void onWorldUnload(WorldUnloadEvent event) {
        PhTreeF<Player> cache = worlds.remove(event.getWorld().getUID());
        if (cache != null) {
            cache.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> PerPlayerMetadata<T> registerMetadata(String key,
            BiConsumer<PerPlayerMetadata<T>, PlayerJoinEvent> onJoin) {
        return (PerPlayerMetadata<T>) metadata.computeIfAbsent(key, (s) -> new PerPlayerMetadata<T>(onJoin));
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

    public static class PerPlayerMetadata<T> {
        private final BiConsumer<PerPlayerMetadata<T>, PlayerJoinEvent> onJoin;
        private final Map<UUID, Map<String, T>> sent = Maps.newHashMap();

        public PerPlayerMetadata(BiConsumer<PerPlayerMetadata<T>, PlayerJoinEvent> onJoin) {
            this.onJoin = onJoin;
        }

        public T getMarker(UUID key, String value) {
            return sent.getOrDefault(key, Collections.emptyMap()).get(value);
        }

        public boolean has(UUID key, String value) {
            return sent.getOrDefault(key, Collections.emptyMap()).containsKey(value);
        }

        public boolean remove(UUID key, String value) {
            return sent.getOrDefault(key, Collections.emptyMap()).remove(value) != null;
        }

        public void set(UUID key, String value, T marker) {
            sent.computeIfAbsent(key, (k) -> Maps.newHashMap()).put(value, marker);
        }
    }
}
