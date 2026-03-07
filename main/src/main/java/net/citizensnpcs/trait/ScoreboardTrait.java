package net.citizensnpcs.trait;

import java.util.Set;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.trait.scoreboard.AbstractScoreboard;
import net.citizensnpcs.trait.scoreboard.AbstractTeam;
import net.citizensnpcs.trait.scoreboard.BukkitScoreboardImpl;
import net.citizensnpcs.trait.scoreboard.FoliaScoreboardImpl;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.LocationLookup.PerPlayerMetadata;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.Util;

@TraitName("scoreboardtrait")
public class ScoreboardTrait extends Trait {
    private boolean changed;
    @Persist
    private ChatColor color;
    private String lastName;
    private final PerPlayerMetadata<Boolean> metadata;
    private ChatColor previousGlowingColor;
    @Persist
    private Set<String> tags = Sets.newHashSet("CITIZENS_NPC");

    private final AbstractScoreboard scoreboard;

    public ScoreboardTrait() {
        super("scoreboardtrait");
        metadata = CitizensAPI.getLocationLookup().<Boolean> registerMetadata("scoreboard", (meta, event) -> {
            for (NPC npc : Iterables.concat(CitizensAPI.getNPCRegistries())) {
                ScoreboardTrait trait = npc.getTraitNullable(ScoreboardTrait.class);
                if (trait == null)
                    continue;

                AbstractTeam team = trait.getTeam();
                if (team == null || meta.has(event.getPlayer().getUniqueId(), team.getName()))
                    continue;

                //NMS.sendTeamPacket(event.getPlayer(), team, 0);
                team.sendToPlayer(event.getPlayer(), AbstractTeam.SendMode.ADD_OR_MODIFY);

                meta.set(event.getPlayer().getUniqueId(), team.getName(), true);
            }
        });

        TeamManager teamManager = ((Citizens) CitizensAPI.getPlugin()).getTeamManager();
        this.scoreboard = SpigotUtil.isFoliaServer() ? new FoliaScoreboardImpl(teamManager) : new BukkitScoreboardImpl();
    }

    private void clearClientTeams(AbstractTeam team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (metadata.remove(player.getUniqueId(), team.getName())) {
                team.sendToPlayer(player, AbstractTeam.SendMode.REMOVE);
                //NMS.sendTeamPacket(player, team, 1);
            }
        }
    }

    public void createTeam(String entityName) {
        String teamName = Util.getTeamName(npc.getUniqueId());
        npc.data().set(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, teamName);
        AbstractTeam team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.createTeam(teamName);
        }
        if (!team.hasEntry(entityName)) {
            clearClientTeams(team);
        }
        team.addEntry(entityName);
    }

    public ChatColor getColor() {
        return color;
    }

    private AbstractTeam getTeam() {
        String teamName = npc.data().get(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, "");
        if (teamName.isEmpty())
            return null;
        return scoreboard.getTeam(teamName);
    }

    @Override
    public void load(DataKey key) {
        if (color != null && color.isFormat()) {
            color = null;
        }
    }

    @Override
    public void onDespawn(DespawnReason reason) {
        previousGlowingColor = null;
        String name = lastName;
        String teamName = npc.data().get(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, "");
        if (teamName.isEmpty())
            return;
        AbstractTeam team = scoreboard.getTeam(teamName);
        npc.data().remove(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME);
        if (team == null || name == null || !team.hasEntry(name)) {
            try {
                if (team != null && team.getSize() == 0) {
                    clearClientTeams(team);
                    scoreboard.removeTeam(teamName);
                }
            } catch (IllegalStateException ex) {
            }
            return;
        }
        Runnable cleanup = () -> {
            if (npc.isSpawned())
                return;
            try {
                team.getSize();
            } catch (IllegalStateException ex) {
                return;
            }
            if (team.getSize() <= 1) {
                clearClientTeams(team);
                scoreboard.removeTeam(teamName);
            } else {
                team.removeEntry(name);
            }
        };
        if (reason == DespawnReason.REMOVAL || reason == DespawnReason.RELOAD) {
            cleanup.run();
        } else {
            CitizensAPI.getScheduler().runEntityTaskLater(npc.getEntity(), cleanup,
                    reason == DespawnReason.DEATH && npc.getEntity() instanceof LivingEntity ? 20 : 2);
        }
    }

    @Override
    public void onRemove() {
        onDespawn(DespawnReason.REMOVAL);
    }

    @Override
    public void onSpawn() {
        changed = true;
        if (SUPPORT_TAGS) {
            npc.getEntity().getScoreboardTags().clear();
            npc.getEntity().getScoreboardTags().addAll(tags);
        }
    }

    public void setColor(ChatColor color) {
        if (color.isFormat())
            throw new IllegalArgumentException();
        this.color = color;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void update() {
        if (SUPPORT_TAGS) {
            if (!npc.getEntity().getScoreboardTags().equals(tags)) {
                tags = Sets.newHashSet(npc.getEntity().getScoreboardTags());
            }
        }
        String forceVisible = npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString();
        boolean nameVisibility = !npc.requiresNameHologram()
                && (forceVisible.equals("true") || forceVisible.equals("hover"));
        AbstractTeam team = getTeam();
        if (team == null)
            return;

        if (!Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                metadata.remove(player.getUniqueId(), team.getName());
                //NMS.sendTeamPacket(player, team, 1);
                team.sendToPlayer(player, AbstractTeam.SendMode.REMOVE);
            }
            //team.unregister();
            scoreboard.removeTeam(team.getName());
            npc.data().remove(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME);
            return;
        }
        if (npc.isSpawned()) {
            lastName = npc.getEntity() instanceof Player && npc.getEntity().getName() != null
                    ? npc.getEntity().getName()
                    : npc.getUniqueId().toString();
        }
        if (SUPPORT_TEAM_SETOPTION) {
            AbstractTeam.NameTags visibility = nameVisibility ? AbstractTeam.NameTags.ALWAYS_SHOW : AbstractTeam.NameTags.NEVER_SHOW;
            if (visibility != team.getNameTagVisibility()) {
                changed = true;
            }
            team.setNameTagVisibility(visibility);
        }
//        else { // TODO
//            NMS.setTeamNameTagVisible(team, nameVisibility);
//        }

        if (SUPPORT_COLLIDABLE_SETOPTION) {
            try {
                AbstractTeam.CollisionRule collide = npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected())
                        ? AbstractTeam.CollisionRule.ALWAYS
                        : AbstractTeam.CollisionRule.NEVER;
                if (collide != team.getCollisionRule()) {
                    changed = true;
                }
                team.setCollisionRule(collide);
                //team.setOption(Option.COLLISION_RULE, collide);
            } catch (NoSuchMethodError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            }
        }
        if (color != null && SUPPORT_GLOWING_COLOR) {
            if (team.getColor() == null || previousGlowingColor == null || color != previousGlowingColor) {
                team.setColor(color);
                previousGlowingColor = color;
                changed = true;
            }
        }
        if (!changed)
            return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC"))
                continue;

            if (metadata.has(player.getUniqueId(), team.getName())) {
                //NMS.sendTeamPacket(player, team, 2);
                team.sendToPlayer(player, AbstractTeam.SendMode.ADD_OR_MODIFY);
            } else {
                //NMS.sendTeamPacket(player, team, 0);
                team.sendToPlayer(player, AbstractTeam.SendMode.ADD_OR_MODIFY);

                metadata.set(player.getUniqueId(), team.getName(), true);
            }
        }
        changed = false;
    }

    private static boolean SUPPORT_COLLIDABLE_SETOPTION = true;
    private static boolean SUPPORT_GLOWING_COLOR = false;
    private static boolean SUPPORT_TAGS = false;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
    static {
        try {
            Entity.class.getDeclaredMethod("getScoreboardTags");
            SUPPORT_TAGS = true;
        } catch (NoSuchMethodException | SecurityException e) {
        }
        try {
            Team.class.getDeclaredMethod("getColor");
            SUPPORT_GLOWING_COLOR = true;
        } catch (NoSuchMethodException | SecurityException e) {
        }
        try {
            OptionStatus status;
        } catch (NoSuchMethodError e) {
            SUPPORT_TEAM_SETOPTION = false;
        } catch (NoClassDefFoundError e) {
            SUPPORT_TEAM_SETOPTION = false;
        }
    }
}
