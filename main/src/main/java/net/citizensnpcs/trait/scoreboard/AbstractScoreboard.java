package net.citizensnpcs.trait.scoreboard;

/**
 * Represents a scoreboard that can be used to manage teams and their entries.
 * This is an abstraction over the underlying {@link org.bukkit.scoreboard.Scoreboard} and
 * {@link net.megavex.scoreboardlibrary.api.team.ScoreboardTeam} objects.
 */
public interface AbstractScoreboard {

    /**
     * Gets a team by name.
     * @param name Name of the team.
     * @return Team with the given name, or null if no such team exists.
     */
    AbstractTeam getTeam(String name);

    /**
     * Deletes a team from this scoreboard.
     * @param name Name of the team.
     */
    void removeTeam(String name);


    /**
     * Creates a new team if it does not exist.
     * @param name Name of the team.
     * @return Team with the given name, or the existing team if it already exists.
     */
    AbstractTeam createTeam(String name);
}
