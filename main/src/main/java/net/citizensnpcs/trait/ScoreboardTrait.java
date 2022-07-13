package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("scoreboardtrait")
public class ScoreboardTrait extends Trait {
    @Persist
    private ChatColor color;
    private int justSpawned;
    private ChatColor previousGlowingColor;
    @Persist
    private final Set<String> tags = new HashSet<String>();

    public ScoreboardTrait() {
        super("scoreboardtrait");
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void apply(boolean nameVisibility) {
        Team team = getTeam();

        if (!Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
            team.unregister();
            npc.data().remove(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
            return;
        }

        Set<String> newTags = new HashSet<String>(tags);
        if (SUPPORT_TAGS) {
            try {
                if (!npc.getEntity().getScoreboardTags().equals(tags)) {
                    justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
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
                    justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
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
                    justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
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
                        justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
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
                    justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
                }
            }
        }
        if (justSpawned > 0) {
            Util.sendTeamPacketToOnlinePlayers(team, 2);
            justSpawned--;
        }
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
        Util.removeTeamFor(npc,
                npc.getEntity() instanceof Player ? npc.getEntity().getName() : npc.getUniqueId().toString());
    }

    @Override
    public void onSpawn() {
        justSpawned = Setting.SCOREBOARD_SEND_TICKS.asInt();
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    private static boolean SUPPORT_COLLIDABLE_SETOPTION = true;
    private static boolean SUPPORT_GLOWING_COLOR = true;
    private static boolean SUPPORT_TAGS = true;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
}
