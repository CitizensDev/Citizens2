package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.Util;

@TraitName("scoreboardtrait")
public class ScoreboardTrait extends Trait {
    private boolean changed;
    @Persist
    private ChatColor color;
    private ChatColor previousGlowingColor;

    @Persist
    private final Set<String> tags = new HashSet<String>();

    public ScoreboardTrait() {
        super("scoreboardtrait");
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void createTeam(String entityName) {
        String teamName = Util.getTeamName(npc.getUniqueId());
        npc.data().set(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, teamName);
        Scoreboard scoreboard = Util.getDummyScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        team.addEntry(entityName);
    }

    public ChatColor getColor() {
        return color;
    }

    public Set<String> getTags() {
        return tags;
    }

    private Team getTeam() {
        String teamName = npc.data().get(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, "");
        if (teamName.isEmpty())
            return null;
        return Util.getDummyScoreboard().getTeam(teamName);
    }

    @Override
    public void onDespawn() {
        if (npc.getEntity() == null)
            return;
        String name = npc.getEntity() instanceof Player ? npc.getEntity().getName() : npc.getUniqueId().toString();
        String teamName = npc.data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, "");
        if (teamName.isEmpty())
            return;
        Team team = Util.getDummyScoreboard().getTeam(teamName);
        npc.data().remove(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
        if (team == null)
            return;
        if (team.hasEntry(name)) {
            if (team.getSize() == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SENT_TEAMS.remove(player.getUniqueId(), team.getName());
                    NMS.sendTeamPacket(player, team, 1);
                }
                team.unregister();
            } else {
                team.removeEntry(name);
            }
        }
    }

    @Override
    public void onSpawn() {
        changed = true;
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void update() {
        String forceVisible = npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString();
        boolean nameVisibility = !npc.requiresNameHologram()
                && (forceVisible.equals("true") || forceVisible.equals("hover"));
        Team team = getTeam();
        if (team == null)
            return;

        if (!Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
            team.unregister();
            npc.data().remove(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
            return;
        }

        Set<String> newTags = new HashSet<String>(tags);
        if (SUPPORT_TAGS) {
            try {
                if (!npc.getEntity().getScoreboardTags().equals(tags)) {
                    changed = true;
                    for (Iterator<String> iterator = npc.getEntity().getScoreboardTags().iterator(); iterator
                            .hasNext();) {
                        String oldTag = iterator.next();
                        if (!newTags.remove(oldTag)) {
                            iterator.remove();
                        }
                    }
                    for (String tag : newTags) {
                        npc.getEntity().addScoreboardTag(tag);
                    }
                }
            } catch (NoSuchMethodError e) {
                SUPPORT_TAGS = false;
            }
        }

        if (SUPPORT_TEAM_SETOPTION) {
            try {
                OptionStatus visibility = nameVisibility ? OptionStatus.ALWAYS : OptionStatus.NEVER;
                if (visibility != team.getOption(Option.NAME_TAG_VISIBILITY)) {
                    changed = true;
                }
                team.setOption(Option.NAME_TAG_VISIBILITY, visibility);
            } catch (NoSuchMethodError e) {
                SUPPORT_TEAM_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_TEAM_SETOPTION = false;
            }
        }

        if (SUPPORT_COLLIDABLE_SETOPTION && npc.data().has(NPC.COLLIDABLE_METADATA)) {
            try {
                OptionStatus collide = npc.data().<Boolean> get(NPC.COLLIDABLE_METADATA) ? OptionStatus.ALWAYS
                        : OptionStatus.NEVER;
                if (collide != team.getOption(Option.COLLISION_RULE)) {
                    changed = true;
                }
                team.setOption(Option.COLLISION_RULE, collide);
            } catch (NoSuchMethodError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            }
        }

        if (!SUPPORT_TEAM_SETOPTION) {
            NMS.setTeamNameTagVisible(team, nameVisibility);
        }

        if (npc.data().has(NPC.GLOWING_COLOR_METADATA)) {
            color = ChatColor.valueOf(npc.data().get(NPC.GLOWING_COLOR_METADATA));
            npc.data().remove(NPC.GLOWING_COLOR_METADATA);
        }

        if (color != null) {
            if (SUPPORT_GLOWING_COLOR && Util.getMinecraftRevision().contains("1_12_R1")) {
                SUPPORT_GLOWING_COLOR = false;
            }
            if (SUPPORT_GLOWING_COLOR) {
                try {
                    if (team.getColor() == null || previousGlowingColor == null
                            || (previousGlowingColor != null && color != previousGlowingColor)) {
                        team.setColor(color);
                        previousGlowingColor = color;
                        changed = true;
                    }
                } catch (NoSuchMethodError err) {
                    SUPPORT_GLOWING_COLOR = false;
                }
            } else {
                if (team.getPrefix() == null || team.getPrefix().length() == 0 || previousGlowingColor == null
                        || (previousGlowingColor != null
                                && !team.getPrefix().equals(previousGlowingColor.toString()))) {
                    team.setPrefix(color.toString());
                    previousGlowingColor = color;
                    changed = true;
                }
            }
        }

        if (changed) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player instanceof NPCHolder)
                    continue;
                if (SENT_TEAMS.containsEntry(player.getUniqueId(), team.getName())) {
                    NMS.sendTeamPacket(player, team, 2);
                } else {
                    NMS.sendTeamPacket(player, team, 0);
                    SENT_TEAMS.put(player.getUniqueId(), team.getName());
                }
            }
        }
    }

    public static void onPlayerJoin(PlayerJoinEvent event) {
        for (Player npcPlayer : PlayerUpdateTask.getCurrentPlayerNPCs()) {
            NPC npc = ((NPCHolder) npcPlayer).getNPC();

            String teamName = npc.data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, "");
            Team team = null;
            if (teamName.length() == 0 || (team = Util.getDummyScoreboard().getTeam(teamName)) == null)
                continue;

            NMS.sendTeamPacket(event.getPlayer(), team, 0);
            SENT_TEAMS.put(event.getPlayer().getUniqueId(), team.getName());
        }
    }

    public static void onPlayerQuit(PlayerQuitEvent event) {
        SENT_TEAMS.removeAll(event.getPlayer().getUniqueId());
    }

    private static SetMultimap<UUID, String> SENT_TEAMS = HashMultimap.create();
    private static boolean SUPPORT_COLLIDABLE_SETOPTION = true;
    private static boolean SUPPORT_GLOWING_COLOR = true;
    private static boolean SUPPORT_TAGS = true;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
}
