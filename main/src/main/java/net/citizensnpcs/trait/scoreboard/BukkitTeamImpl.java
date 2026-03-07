package net.citizensnpcs.trait.scoreboard;

import net.citizensnpcs.util.NMS;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class BukkitTeamImpl implements AbstractTeam {

    private final Team delegate;

    public BukkitTeamImpl(Team delegate) {
        this.delegate = delegate;
    }


    @Override
    public void addEntry(String entry) {
        delegate.addEntry(entry);
    }

    @Override
    public boolean hasEntry(String entry) {
        return delegate.hasEntry(entry);
    }

    @Override
    public void removeEntry(String entry) {
        delegate.removeEntry(entry);
    }

    @Override
    public ChatColor getColor() {
        return delegate.getColor();
    }

    @Override
    public NameTags getNameTagVisibility() {
        return NameTags.fromBukkit(delegate.getOption(Team.Option.NAME_TAG_VISIBILITY));
    }

    @Override
    public CollisionRule getCollisionRule() {
        return CollisionRule.fromBukkit(delegate.getOption(Team.Option.COLLISION_RULE));
    }

    @Override
    public void setColor(ChatColor color) {
        delegate.setColor(color);
    }

    @Override
    public void setNameTagVisibility(NameTags visibility) {

        Team.OptionStatus optionStatus = visibility.getBukkitValue();
        delegate.setOption(Team.Option.NAME_TAG_VISIBILITY, optionStatus);
    }

    @Override
    public void setCollisionRule(CollisionRule rule) {
        Team.OptionStatus optionStatus = rule.getBukkitValue();
        delegate.setOption(Team.Option.COLLISION_RULE, optionStatus);
    }

    @Override
    public Team getDelegate() {
        return delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void sendToPlayer(Player player, SendMode mode) {
        NMS.sendTeamPacket(player, delegate, mode.getValue());
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }
}
