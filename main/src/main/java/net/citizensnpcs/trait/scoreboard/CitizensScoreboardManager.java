package net.citizensnpcs.trait.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface CitizensScoreboardManager {
    void addPlayer(Player player);

    void close();

    AbstractScoreboard createScoreboard();

    <T> T getTeamManager();

    void load(Plugin plugin);

    void removePlayer(Player player);
}
