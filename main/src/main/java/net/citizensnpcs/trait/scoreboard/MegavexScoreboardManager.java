package net.citizensnpcs.trait.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.util.SpigotUtil;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.megavex.scoreboardlibrary.implementation.ScoreboardLibraryImpl;

public class MegavexScoreboardManager implements CitizensScoreboardManager {
    private ScoreboardLibrary scoreboardLibrary;
    private final TeamManager teamManager;

    public MegavexScoreboardManager(Plugin plugin) {
        try {
            scoreboardLibrary = new ScoreboardLibraryImpl(plugin);
        } catch (NoPacketAdapterAvailableException e) {
            scoreboardLibrary = new NoopScoreboardLibrary();
        }
        teamManager = scoreboardLibrary.createTeamManager();
    }

    @Override
    public void addPlayer(Player player) {
        teamManager.addPlayer(player);
    }

    @Override
    public void close() {
        scoreboardLibrary.close();
    }

    @Override
    public AbstractScoreboard createScoreboard() {
        return SpigotUtil.isFoliaServer() ? new FoliaScoreboardImpl(teamManager) : new BukkitScoreboardImpl();
    }

    @Override
    public void removePlayer(Player player) {
        teamManager.removePlayer(player);
    }
}
