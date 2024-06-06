package net.citizensnpcs.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ch.ethz.globis.phtree.PhTreeF;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

public class LocationLookup extends BukkitRunnable {
    private final Map<String, PerPlayerMetadata<?>> metadata = Maps.newHashMap();
    private Future<Map<UUID, PhTreeF<NPC>>> npcFuture = null;
    private Map<UUID, PhTreeF<NPC>> npcWorlds = Maps.newHashMap();
    private Future<Map<UUID, PhTreeF<Player>>> playerFuture = null;
    private final NPCRegistry sourceRegistry;
    private Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public LocationLookup() {
        this(CitizensAPI.getNPCRegistry());
    }

    public LocationLookup(NPCRegistry sourceRegistry) {
        this.sourceRegistry = sourceRegistry;
    }

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

    public Iterable<Player> getNearbyPlayers(World base, double[] min, double[] max) {
        PhTreeF<Player> tree = worlds.get(base.getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.query(min, max);
    }

    public Iterable<Player> getNearbyVisiblePlayers(Entity entity, double range) {
        return getNearbyVisiblePlayers(entity, entity.getLocation(), range);
    }

    public Iterable<Player> getNearbyVisiblePlayers(Entity base, double[] min, double[] max) {
        return filterToVisiblePlayers(base, getNearbyPlayers(base.getWorld(), min, max));
    }

    public Iterable<Player> filterToVisiblePlayers(Entity base, Iterable<Player> players) {
        Player player = base instanceof Player ? (Player) base : null;
        return Iterables.filter(players, other -> {
            boolean canSee = true;
            if (SUPPORTS_ENTITY_CANSEE) {
                try {
                    canSee = other.canSee(base);
                } catch (NoSuchMethodError t) {
                    SUPPORTS_ENTITY_CANSEE = false;
                    if (player != null) {
                        canSee = other.canSee(player);
                    }
                }
            } else if (player != null) {
                canSee = other.canSee(player);
            }
            return other.getWorld() == base.getWorld() && canSee
                    && !other.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    && other.getGameMode() != GameMode.SPECTATOR;
        });
    }

    public Iterable<Player> getNearbyVisiblePlayers(Entity base, Location location, double range) {
        return filterToVisiblePlayers(base, getNearbyPlayers(location, range));
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
        PhTreeF<NPC> npcCache = npcWorlds.remove(event.getWorld().getUID());
        if (npcCache != null) {
            npcCache.clear();
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
            for (NPC npc : sourceRegistry) {
                if (!npc.isSpawned())
                    continue;
                npc.getEntity().getLocation(loc);
                Collection<TreeFactory.Node<NPC>> nodes = map.computeIfAbsent(npc.getEntity().getWorld().getUID(),
                        uid -> Lists.newArrayList());
                nodes.add(new TreeFactory.Node<>(new double[] { loc.getX(), loc.getY(), loc.getZ() }, npc));
            }
            npcFuture = ForkJoinPool.commonPool().submit(new TreeFactory<>(map));
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
            playerFuture = ForkJoinPool.commonPool().submit(new TreeFactory<>(map));
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

    // TODO: separate out NPCs and Player lookups into this
    public static abstract class AsyncPhTreeLoader<K, V> implements Runnable {
        private Future<Map<K, PhTreeF<V>>> future;
        protected Map<K, PhTreeF<V>> mapping = Maps.newHashMap();

        protected abstract Map<K, Collection<TreeFactory.Node<V>>> generateLoaderMap();

        public Iterable<V> getNearby(K lookup, double dist, double[] center) {
            PhTreeF<V> tree = mapping.get(lookup);
            if (tree == null)
                return Collections.emptyList();
            return () -> tree.rangeQuery(dist, center);
        }

        public Iterable<V> getNearby(K lookup, double[] min, double[] max) {
            PhTreeF<V> tree = mapping.get(lookup);
            if (tree == null)
                return Collections.emptyList();
            return () -> tree.query(min, max);
        }

        @Override
        public void run() {
            if (future != null && future.isDone()) {
                try {
                    mapping = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                future = null;
            }
            if (future == null) {
                future = ForkJoinPool.commonPool().submit(new TreeFactory<>(generateLoaderMap()));
            }
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

    private static final class TreeFactory<K, V> implements Callable<Map<K, PhTreeF<V>>> {
        private final Map<K, Collection<Node<V>>> source;

        public TreeFactory(Map<K, Collection<Node<V>>> source) {
            this.source = source;
        }

        @Override
        public Map<K, PhTreeF<V>> call() throws Exception {
            Map<K, PhTreeF<V>> result = Maps.newHashMap();
            for (K k : source.keySet()) {
                PhTreeF<V> tree = PhTreeF.create(3);
                for (Node<V> entry : source.get(k)) {
                    tree.put(entry.loc, entry.t);
                }
                result.put(k, tree);
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

    private static boolean SUPPORTS_ENTITY_CANSEE = true;
}
