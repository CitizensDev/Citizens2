package net.citizensnpcs.trait.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BukkitScoreboardManager implements CitizensScoreboardManager {
    public BukkitScoreboardManager(Plugin plugin) {
    }

    @Override
    public void addPlayer(Player player) {
    }

    @Override
    public void close() {
    }

    @Override
    public AbstractScoreboard createScoreboard() {
        return new BukkitScoreboardImpl();
    }

    @Override
    public void removePlayer(Player player) {
    }
}
