package net.citizensnpcs.trait.scoreboard;

import org.bukkit.entity.Player;

public interface CitizensScoreboardManager {
    void addPlayer(Player player);

    void close();

    AbstractScoreboard createScoreboard();

    void removePlayer(Player player);
}
