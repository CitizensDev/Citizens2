package net.citizensnpcs.trait.scoreboard;

import net.citizensnpcs.util.Util;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

public class BukkitScoreboardImpl implements AbstractScoreboard {


    private final Scoreboard scoreboard;

    public BukkitScoreboardImpl() {
        this.scoreboard = Util.getDummyScoreboard();
    }

    @Nullable
    @Override
    public AbstractTeam getTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            return new BukkitTeamImpl(team);
        }
        return null;
    }

    @Override
    public void removeTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.unregister();
        }
    }

    @Override
    public AbstractTeam createTeam(String name) {
        return new BukkitTeamImpl(scoreboard.registerNewTeam(name));
    }
}
