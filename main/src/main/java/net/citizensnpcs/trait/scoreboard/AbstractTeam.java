package net.citizensnpcs.trait.scoreboard;

import net.megavex.scoreboardlibrary.api.team.enums.NameTagVisibility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;


/**
 * Represents a team that can be used to manage entries and their display properties.
 * This is an abstraction over the underlying {@link Team} and {@link net.megavex.scoreboardlibrary.api.team.ScoreboardTeam} objects.
 * Entries can be either player display names (usernames) or entity UUIDs represented as strings.
 */
public interface AbstractTeam {

    /**
     * Adds an entry to the team.
     * For players, this should be their display name (username).
     * For entities, this should be their UUID represented as a string.
     *
     * @param entry Entry to add.
     */
    void addEntry(String entry);

    /**
     * Checks if the team has an entry.
     * For players, this should be their display name (username).
     * For entities, this should be their UUID represented as a string.
     *
     * @param entry Entry to check.
     * @return True if the team has the entry, false otherwise.
     */
    boolean hasEntry(String entry);

    /**
     * Removes an entry from the team.
     * For players, this should be their display name (username).
     * For entities, this should be their UUID represented as a string.
     *
     * @param entry Entry to remove.
     */
    void removeEntry(String entry);

    /**
     * Gets the color of the team.
     * @return The color of the team.
     */
    ChatColor getColor();

    /**
     * Gets the name tag visibility of the team.
     * @return The name tag visibility of the team.
     */
    NameTags getNameTagVisibility();

    /**
     * Gets the collision rule of the team.
     * @return The collision rule of the team.
     */
    CollisionRule getCollisionRule();

    /**
     * Sets the color of the team.
     * @param color The color of the team.
     */
    void setColor(ChatColor color);

    /**
     * Sets the name tag visibility of the team.
     * @param visibility The name tag visibility of the team.
     */
    void setNameTagVisibility(NameTags visibility);

    /**
     * Sets the collision rule of the team.
     * @param rule The collision rule of the team.
     */
    void setCollisionRule(CollisionRule rule);

    /**
     * Gets the underlying delegate object.
     * This may either be a {@link Team} or {@link net.megavex.scoreboardlibrary.api.team.ScoreboardTeam}
     * @return The underlying delegate object.
     */
    Object getDelegate();

    /**
     * Gets the name of the team.
     * @return The name of the team.
     */
    String getName();

    /**
     * Sends the team to a player.
     * @param player Player to send to.
     * @param mode Send mode.
     */
    void sendToPlayer(Player player, SendMode mode);

    /**
     * Gets the number of entries in the team.
     * @return The number of entries in the team.
     */
    int getSize();


    /**
     * Interface for team options that can be represented as both a packet-based value and a Bukkit value.
     * @param <P> Packet-based (folia) value type.
     */
    interface TeamOption<P> {

        /**
         * Gets the packet-based (folia) value of this option.
         * @return The packet-based (folia) value of this option.
         */
        P getFoliaValue();

        /**
         * Gets the Bukkit value of this option.
         * @return The Bukkit value of this option.
         */
        Team.OptionStatus getBukkitValue();

        /**
         * Utility method to convert from a packet-based (folia) value to the corresponding enum constant.
         * @param values Array of enum constants.
         * @param foliaValue Packet-based (folia) value to convert.
         * @return The corresponding enum constant.
         * @param <E> Enum type.
         * @param <P> Packet-based (folia) value type.
         */
        static <E extends TeamOption<P>, P> E fromFolia(E[] values, P foliaValue) {
            for (E e : values) {
                if (e.getFoliaValue().equals(foliaValue)) return e;
            }
            return values[0];
        }

        /**
         * Utility method to convert from a Bukkit value to the corresponding enum constant.
         * @param values Array of enum constants.
         * @param bukkitValue Bukkit value to convert.
         * @return The corresponding enum constant.
         * @param <E> Enum type.
         */
        static <E extends TeamOption<?>> E fromBukkit(E[] values, Team.OptionStatus bukkitValue) {
            for (E e : values) {
                if (e.getBukkitValue() == bukkitValue) return e;
            }
            return values[0];
        }
    }

    /**
     * Enum representing name tag visibility options.
     */
    enum NameTags implements TeamOption<NameTagVisibility> {
        ALWAYS_SHOW(NameTagVisibility.ALWAYS, Team.OptionStatus.ALWAYS),
        NEVER_SHOW(NameTagVisibility.NEVER, Team.OptionStatus.NEVER),
        HIDE_FOR_OTHER_TEAMS(NameTagVisibility.HIDE_FOR_OTHER_TEAMS, Team.OptionStatus.FOR_OTHER_TEAMS),
        HIDE_FOR_OWN_TEAM(NameTagVisibility.HIDE_FOR_OWN_TEAM, Team.OptionStatus.FOR_OWN_TEAM);

        private final NameTagVisibility packetBasedValue;
        private final Team.OptionStatus bukkitValue;

        NameTags(NameTagVisibility packetBasedValue, Team.OptionStatus bukkitValue) {
            this.packetBasedValue = packetBasedValue;
            this.bukkitValue = bukkitValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NameTagVisibility getFoliaValue() {
            return packetBasedValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Team.OptionStatus getBukkitValue() {
            return bukkitValue;
        }

        /**
         * @see #fromPacket(NameTagVisibility)
         */
        public static NameTags fromPacket(NameTagVisibility value) {
            return TeamOption.fromFolia(values(), value);
        }

        /**
         * @see #fromBukkit(Team.OptionStatus)
         */
        public static NameTags fromBukkit(Team.OptionStatus value) {
            return TeamOption.fromBukkit(values(), value);
        }
    }


    /**
     * Enum representing collision rule options.
     */
    enum CollisionRule implements TeamOption<net.megavex.scoreboardlibrary.api.team.enums.CollisionRule> {
        ALWAYS(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule.ALWAYS, Team.OptionStatus.ALWAYS),
        NEVER(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule.NEVER, Team.OptionStatus.NEVER),
        PUSH_OTHER_TEAMS(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule.PUSH_OTHER_TEAMS, Team.OptionStatus.FOR_OTHER_TEAMS),
        PUSH_OWN_TEAM(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule.PUSH_OWN_TEAM, Team.OptionStatus.FOR_OWN_TEAM);

        private final net.megavex.scoreboardlibrary.api.team.enums.CollisionRule packetBasedValue;
        private final Team.OptionStatus bukkitValue;

        CollisionRule(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule packetBasedValue, Team.OptionStatus bukkitValue) {
            this.packetBasedValue = packetBasedValue;
            this.bukkitValue = bukkitValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public net.megavex.scoreboardlibrary.api.team.enums.CollisionRule getFoliaValue() {
            return packetBasedValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Team.OptionStatus getBukkitValue() {
            return bukkitValue;
        }

        /**
         * @see #fromBukkit(Team.OptionStatus)
         */
        public static CollisionRule fromPacket(net.megavex.scoreboardlibrary.api.team.enums.CollisionRule value) {
            return TeamOption.fromFolia(values(), value);
        }

        /**
         * @see #fromBukkit(Team.OptionStatus)
         */
        public static CollisionRule fromBukkit(Team.OptionStatus value) {
            return TeamOption.fromBukkit(values(), value);
        }
    }

    /**
     * Enum representing send modes.
     */
    enum SendMode {
        ADD_OR_MODIFY(0),
        REMOVE(1);

        private final int value;

        SendMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
