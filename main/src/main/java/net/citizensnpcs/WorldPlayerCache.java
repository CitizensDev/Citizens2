package net.citizensnpcs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import ch.ethz.globis.phtree.PhTreeF;

public class WorldPlayerCache implements Runnable {
    private final Map<UUID, PhTreeF<Player>> worlds = Maps.newHashMap();

    public PhTreeF<Player> getPlayersByWorld(World world) {
        return worlds.get(world.getUID());
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
