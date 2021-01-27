package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

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
    private boolean justSpawned;
    private ChatColor previousGlowingColor;
    @Persist
    private final Set<String> tags = new HashSet<String>();

    public ScoreboardTrait() {
        super("scoreboardtrait");
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void apply(Team team, boolean nameVisibility) {
        boolean changed = false;
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
        if (changed || justSpawned) {
            Util.sendTeamPacketToOnlinePlayers(team, 2);
            justSpawned = false;
        }
    }

    public ChatColor getColor() {
        return color;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public void onSpawn() {
        justSpawned = true;
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    private static boolean SUPPORT_GLOWING_COLOR = true;
    private static boolean SUPPORT_TAGS = true;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
}
