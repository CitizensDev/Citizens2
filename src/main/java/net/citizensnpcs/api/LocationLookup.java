package net.citizensnpcs.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ch.ethz.globis.phtree.PhTreeF;
import net.citizensnpcs.api.npc.NPC;

public class LocationLookup extends BukkitRunnable {
    private final ExecutorService async = Executors.newSingleThreadExecutor();
    private final Map<String, PerPlayerMetadata<?>> metadata = Maps.newHashMap();
    private Future<Map<UUID, PhTreeF<NPC>>> npcFuture = null;
    private Map<UUID, PhTreeF<NPC>> npcWorlds = Maps.newHashMap();
    private Future<Map<UUID, PhTreeF<Player>>> playerFuture = null;
    private Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public PerPlayerMetadata<?> getMetadata(String key) {
        return metadata.get(key);
    }

    public Iterable<NPC> getNearbyNPCs(Location base, double dist) {
        PhTreeF<NPC> tree = npcWorlds.get(base.getWorld().getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.rangeQuery(dist, base.getX(), base.getY(), base.getZ());
    }

    public Iterable<NPC> getNearbyNPCs(NPC npc) {
        return getNearbyNPCs(npc.getStoredLocation(), npc.data().get(NPC.Metadata.TRACKING_RANGE, 64));
    }

    public Iterable<NPC> getNearbyNPCs(World world, double[] min, double[] max) {
        PhTreeF<NPC> tree = npcWorlds.get(world.getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.query(min, max);
    }

    public Iterable<Player> getNearbyPlayers(Location base, double dist) {
        PhTreeF<Player> tree = worlds.get(base.getWorld().getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.rangeQuery(dist, base.getX(), base.getY(), base.getZ());
    }

    public Iterable<Player> getNearbyPlayers(NPC npc) {
        return getNearbyPlayers(npc.getStoredLocation(), npc.data().get(NPC.Metadata.TRACKING_RANGE, 64));
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
        return (PerPlayerMetadata<T>) metadata.computeIfAbsent(key, s -> new PerPlayerMetadata<>(onJoin));
    }

    @Override
    public void run() {
        if (npcFuture != null && npcFuture.isDone()) {
            try {
                npcWorlds = npcFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            npcFuture = null;
        }
        if (npcFuture == null) {
            Map<UUID, Collection<TreeFactory.Node<NPC>>> map = Maps.newHashMap();
            Location loc = new Location(null, 0, 0, 0);
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (!npc.isSpawned())
                    continue;
                npc.getEntity().getLocation(loc);
                Collection<TreeFactory.Node<NPC>> nodes = map.computeIfAbsent(npc.getEntity().getWorld().getUID(),
                        uid -> Lists.newArrayList());
                nodes.add(new TreeFactory.Node<>(new double[] { loc.getX(), loc.getY(), loc.getZ() }, npc));
            }
            npcFuture = async.submit(new TreeFactory<>(map));
        }
        if (playerFuture != null && playerFuture.isDone()) {
            try {
                worlds = playerFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            playerFuture = null;
        }
        if (playerFuture == null) {
            Map<UUID, Collection<TreeFactory.Node<Player>>> map = Maps.newHashMap();
            Location loc = new Location(null, 0, 0, 0);
            for (World world : Bukkit.getServer().getWorlds()) {
                Collection<Player> players = Collections2.filter(world.getPlayers(), p -> !p.hasMetadata("NPC"));
                if (players.isEmpty())
                    continue;
                map.put(world.getUID(), Collections2.transform(players, p -> {
                    p.getLocation(loc);
                    return new TreeFactory.Node<>(new double[] { loc.getX(), loc.getY(), loc.getZ() }, p);
                }));
            }
            playerFuture = async.submit(new TreeFactory<>(map));
        }
    }

    // TODO: remove?
    private void updateWorld(World world) {
        Collection<Player> players = Collections2.filter(world.getPlayers(), p -> !p.hasMetadata("NPC"));
        if (players.isEmpty()) {
            worlds.remove(world.getUID());
            return;
        }
        PhTreeF<Player> tree = worlds.computeIfAbsent(world.getUID(), uid -> PhTreeF.create(3));
        tree.clear();
        Location loc = new Location(null, 0, 0, 0);
        for (Player player : players) {
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

        public void removeAllValues(String value) {
            for (Map<String, T> map : sent.values()) {
                map.remove(value);
            }
        }

        public void set(UUID key, String value, T marker) {
            if (marker instanceof Location || marker instanceof World)
                throw new IllegalArgumentException("Invalid marker");
            sent.computeIfAbsent(key, k -> Maps.newHashMap()).put(value, marker);
        }
    }

    private static final class TreeFactory<T> implements Callable<Map<UUID, PhTreeF<T>>> {
        private final Map<UUID, Collection<Node<T>>> source;

        public TreeFactory(Map<UUID, Collection<Node<T>>> source) {
            this.source = source;
        }

        @Override
        public Map<UUID, PhTreeF<T>> call() throws Exception {
            Map<UUID, PhTreeF<T>> result = Maps.newHashMap();
            for (UUID uuid : source.keySet()) {
                PhTreeF<T> tree = PhTreeF.create(3);
                for (Node<T> entry : source.get(uuid)) {
                    tree.put(entry.loc, entry.t);
                }
                result.put(uuid, tree);
            }
            return result;
        }

        public static class Node<T> {
            public double[] loc;
            public T t;

            public Node(double[] loc, T t) {
                this.loc = loc;
                this.t = t;
            }
        }
    }
}
