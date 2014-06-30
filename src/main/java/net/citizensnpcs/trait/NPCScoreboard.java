package net.citizensnpcs.trait;

import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Sets;

public class NPCScoreboard extends Trait {
    private final Set<Objective> objectives = Sets.newHashSet();
    private Scoreboard scoreboard;
    private String team;

    public NPCScoreboard() {
        super("npcscoreboard");
    }

    private void checkScoreboard() {
        if (scoreboard == null) {
            if (npc.isSpawned() && !npc.getEntity().getMetadata("citizens.scoreboard").isEmpty()) {
                scoreboard = (Scoreboard) npc.getEntity().getMetadata("citizens.scoreboard").iterator().next().value();
            } else {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            }
        }
        if (npc.getEntity() instanceof Player && npc.getEntity().getMetadata("citizens.scoreboard").isEmpty()) {
            npc.getEntity().setMetadata("citizens.scoreboard",
                    new FixedMetadataValue(CitizensAPI.getPlugin(), scoreboard));
        }
    }

    @Override
    public void load(DataKey root) {
        checkScoreboard();
        if (root.keyExists("team")) {
            team = root.getString("team");
        }
        if (!root.keyExists("objectives"))
            return;
        for (DataKey sub : root.getRelative("objectives").getSubKeys()) {
            String objName = sub.name();
            Objective objective;
            if (scoreboard.getObjective(objName) != null) {
                objective = scoreboard.getObjective(objName);
            } else {
                objective = scoreboard.registerNewObjective(objName, root.getString("criteria"));
            }
            objective.setDisplaySlot(DisplaySlot.valueOf(sub.getString("display")));
            objectives.add(objective);
            if (npc.isSpawned() && npc.getEntity() instanceof Player) {
                objective.getScore((Player) npc.getEntity()).setScore(sub.getInt("score"));
            }
        }
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Player && team != null) {
            checkScoreboard();
            scoreboard.getTeam(team).addPlayer((Player) npc.getEntity());
        }
    }

    public void persistObjective(Objective objective) {
        if (!objectives.contains(objective)) {
            objectives.add(objective);
        }
        Team playerTeam = objectives.iterator().next().getScoreboard().getPlayerTeam((Player) npc.getEntity());
        if (team == null && playerTeam != null) {
            team = playerTeam.getName();
        }
        if (scoreboard == null) {
            scoreboard = objective.getScoreboard();
        }
    }

    @Override
    public void save(DataKey root) {
        root.removeKey("objectives");
        root.removeKey("team");
        if (objectives.isEmpty()) {
            return;
        }
        for (Objective objective : objectives) {
            DataKey key = root.getRelative("objectives." + objective.getName());
            key.setString("criteria", objective.getCriteria());
            key.setString("display", objective.getDisplaySlot().name());
            if (npc.getEntity() instanceof Player) {
                key.setInt("score", objective.getScore((Player) npc.getEntity()).getScore());
            }
        }
        if (npc.getEntity() instanceof Player && team == null) {
            Team playerTeam = objectives.iterator().next().getScoreboard().getPlayerTeam((Player) npc.getEntity());
            if (playerTeam != null) {
                team = playerTeam.getName();
            }
        }
        if (team != null) {
            root.setString("team", team);
        }
    }
}