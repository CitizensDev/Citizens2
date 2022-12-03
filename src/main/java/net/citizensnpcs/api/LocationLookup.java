package net.citizensnpcs.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import ch.ethz.globis.phtree.PhTreeF;

public class LocationLookup implements Runnable {
    private final Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public Iterable<Player> getNearbyPlayers(Location base, double dist) {
        PhTreeF<Player> tree = worlds.get(base.getWorld().getUID());
        if (tree == null)
            return Collections.emptyList();
        return () -> tree.rangeQuery(dist, base.getX(), base.getY(), base.getZ());
    }

    @Override
    public void run() {
        worlds.clear();
        for (World world : Bukkit.getServer().getWorlds()) {
            List<Player> players = world.getPlayers();
            if (players.isEmpty())
                continue;
            PhTreeF<Player> tree = PhTreeF.create(3);
            worlds.put(world.getUID(), tree);
            Location loc = new Location(null, 0, 0, 0);
            for (Player player : players) {
                if (player.hasMetadata("NPC"))
                    continue;
                player.getLocation(loc);
                tree.put(new double[] { loc.getX(), loc.getY(), loc.getZ() }, player);
            }
        }
    }
}
