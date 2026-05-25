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
    private TeamManager teamManager;

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
        return SpigotUtil.isFoliaServer() ? new FoliaScoreboardImpl(this) : new BukkitScoreboardImpl();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTeamManager() {
        return (T) teamManager;
    }

    @Override
    public void load(Plugin plugin) {
        try {
            scoreboardLibrary = new ScoreboardLibraryImpl(plugin);
        } catch (NoPacketAdapterAvailableException e) {
            scoreboardLibrary = new NoopScoreboardLibrary();
        }
        teamManager = scoreboardLibrary.createTeamManager();
    }

    @Override
    public void removePlayer(Player player) {
        teamManager.removePlayer(player);
    }
}
