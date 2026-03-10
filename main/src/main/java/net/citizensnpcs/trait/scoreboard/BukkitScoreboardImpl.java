package net.citizensnpcs.trait.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;

public class BukkitScoreboardImpl implements AbstractScoreboard {
    private static Scoreboard DUMMY_SCOREBOARD;

    private final Scoreboard delegate;

    public BukkitScoreboardImpl() {
        this.delegate = getDummyScoreboard();
    }

    @Nullable
    @Override
    public AbstractTeam getTeam(String name) {
        Team team = delegate.getTeam(name);
        if (team != null) {
            return new BukkitTeamImpl(team);
        }
        return null;
    }

    @Override
    public void removeTeam(String name) {
        Team team = delegate.getTeam(name);
        if (team != null) {
            team.unregister();
        }
    }

    @Override
    public AbstractTeam createTeam(String name) {
        return new BukkitTeamImpl(delegate.registerNewTeam(name));
    }

    private static Scoreboard getDummyScoreboard() {
        if (DUMMY_SCOREBOARD == null) {
            DUMMY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        return DUMMY_SCOREBOARD;
    }
}
