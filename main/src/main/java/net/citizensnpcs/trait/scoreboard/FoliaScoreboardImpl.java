package net.citizensnpcs.trait.scoreboard;

import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamManager;

public class FoliaScoreboardImpl implements AbstractScoreboard {
    private final TeamManager delegate;

    public FoliaScoreboardImpl(TeamManager teamManager) {
        this.delegate = teamManager;
    }

    @Override
    public AbstractTeam createTeam(String name) {
        return new FoliaTeamImpl(delegate.createIfAbsent(name));
    }

    @Override
    public AbstractTeam getTeam(String name) {
        ScoreboardTeam scoreboardTeam = delegate.team(name);
        return scoreboardTeam != null ? new FoliaTeamImpl(scoreboardTeam) : null;
    }

    @Override
    public void removeTeam(String name) {
        delegate.removeTeam(name);
    }
}
