package net.citizensnpcs.trait.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.enums.NameTagVisibility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class FoliaTeamImpl implements AbstractTeam {


    private final ScoreboardTeam delegateTeam;
    private final TeamDisplay delegateDisplay;

    public FoliaTeamImpl(ScoreboardTeam delegateTeam) {
        this.delegateTeam = delegateTeam;
        this.delegateDisplay = delegateTeam.defaultDisplay();
    }


    @Override
    public void addEntry(String entry) {
        delegateDisplay.addEntry(entry);
    }

    @Override
    public boolean hasEntry(String entry) {
        return delegateDisplay.entries().contains(entry);
    }

    @Override
    public void removeEntry(String entry) {
        delegateDisplay.removeEntry(entry);
    }

    @Override
    public ChatColor getColor() {
        return toChatColor(delegateDisplay.playerColor());
    }

    @Override
    public NameTags getNameTagVisibility() {
        return NameTags.fromPacket(delegateDisplay.nameTagVisibility());
    }

    @Override
    public CollisionRule getCollisionRule() {
        return CollisionRule.fromPacket(delegateDisplay.collisionRule());
    }

    @Override
    public void setColor(ChatColor color) {
        delegateDisplay.playerColor(toNamedTextColor(color));
    }

    @Override
    public void setNameTagVisibility(NameTags visibility) {
        NameTagVisibility value = visibility.getFoliaValue();
        delegateDisplay.nameTagVisibility(value);
    }

    @Override
    public void setCollisionRule(CollisionRule rule) {
        net.megavex.scoreboardlibrary.api.team.enums.CollisionRule value = rule.getFoliaValue();
        delegateDisplay.collisionRule(value);
    }

    @Override
    public Object getDelegate() {
        return delegateTeam;
    }

    @Override
    public String getName() {
        return delegateTeam.name();
    }

    @Override
    public int getSize() {
        return delegateDisplay.entries().size();
    }

    @Override
    public void sendToPlayer(Player player, SendMode mode) {
        // TODO: Currently not implemented
        /*if (mode == SendMode.ADD_OR_MODIFY) {
            delegateDisplay.refresh();
            delegateTeam.display(player, delegateDisplay);
        } else {
            delegateTeam.display(player, delegateTeam.defaultDisplay());
        }*/
    }

    private ChatColor toChatColor(@Nullable NamedTextColor color) {
        if (color == null) return null;
        return ChatColor.valueOf(color.toString().toUpperCase(Locale.ROOT));
    }

    private NamedTextColor toNamedTextColor(@Nullable ChatColor color) {
        if (color == null) return null;
        return NamedTextColor.NAMES.value(color.name().toLowerCase(Locale.ROOT));
    }
}
