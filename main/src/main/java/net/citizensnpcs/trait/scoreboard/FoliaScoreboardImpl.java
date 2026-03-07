package net.citizensnpcs.trait.scoreboard;

import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamManager;

public class FoliaScoreboardImpl implements AbstractScoreboard {


    private final TeamManager delegate;

    public FoliaScoreboardImpl(TeamManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public AbstractTeam getTeam(String name) {
        ScoreboardTeam scoreboardTeam = delegate.team(name);
        if (scoreboardTeam != null) {
            return new FoliaTeamImpl(scoreboardTeam);
        }
        return null;
    }

    @Override
    public void removeTeam(String name) {
        delegate.removeTeam(name);
    }

    @Override
    public AbstractTeam createTeam(String name) {
        return new FoliaTeamImpl(delegate.createIfAbsent(name));
    }
}
