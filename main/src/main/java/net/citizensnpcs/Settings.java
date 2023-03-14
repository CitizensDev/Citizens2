package net.citizensnpcs;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.Util;

public class Settings {
    private final Storage config;
    private final DataKey root;

    public Settings(File folder) {
        config = new YamlStorage(new File(folder, "config.yml"), "Citizens Configuration");
        root = config.getKey("");

        config.load();
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                setting.setAtKey(root);
            } else {
                setting.loadFromKey(root);
            }
        }
        updateMessagingSettings();
        save();
    }

    public void reload() {
        config.load();
        for (Setting setting : Setting.values()) {
            if (root.keyExists(setting.path)) {
                setting.loadFromKey(root);
            }
        }
        updateMessagingSettings();
        save();
    }

    public void save() {
        config.save();
    }

    private void updateMessagingSettings() {
        File file = null;
        if (!Setting.DEBUG_FILE.asString().isEmpty()) {
            file = new File(CitizensAPI.getPlugin().getDataFolder(), Setting.DEBUG_FILE.asString());
        }
        Messaging.configure(file, Setting.DEBUG_MODE.asBoolean(), Setting.MESSAGE_COLOUR.asString(),
                Setting.HIGHLIGHT_COLOUR.asString(), Setting.ERROR_COLOUR.asString());
    }

    public enum Setting {
        ALWAYS_USE_NAME_HOLOGRAM("npc.always-use-name-holograms", false),
        ASTAR_ITERATIONS_PER_TICK("npc.pathfinding.new-finder.iterations-per-tick",
                "npc.pathfinding.new-finder.iterations-per-tick", 2500),
        AUTH_SERVER_URL("general.authlib.profile-url", "https://sessionserver.mojang.com/session/minecraft/profile/"),
        BOSSBAR_RANGE("npc.default.bossbar-view-range", 64),
        CHAT_BYSTANDERS_HEAR_TARGETED_CHAT("npc.chat.options.bystanders-hear-targeted-chat", false),
        CHAT_FORMAT("npc.chat.format.no-targets", "[<npc>]: <text>"),
        CHAT_FORMAT_TO_BYSTANDERS("npc.chat.format.with-target-to-bystanders", "[<npc>] -> [<target>]: <text>"),
        CHAT_FORMAT_TO_TARGET("npc.chat.format.to-target", "<npc>: <text>"),
        CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS("npc.chat.format.with-targets-to-bystanders",
                "[<npc>] -> [<targets>]: <text>"),
        CHAT_MAX_NUMBER_OF_TARGETS("npc.chat.options.max-number-of-targets-to-show", 2),
        CHAT_MULTIPLE_TARGETS_FORMAT("npc.chat.options.multiple-targets-format",
                "<target>|, <target>| & <target>| & others"),
        CHAT_RANGE("npc.chat.options.range", 5),
        CHECK_MINECRAFT_VERSION("advanced.check-minecraft-version", true),
        CONTROLLABLE_GROUND_DIRECTION_MODIFIER("npc.controllable.ground-direction-modifier", 1.0D),
        DEBUG_CHUNK_LOADS("general.debug-chunk-loads", false),
        DEBUG_FILE("general.debug-file", ""),
        DEBUG_MODE("general.debug-mode", false),
        DEBUG_PATHFINDING("general.debug-pathfinding", false),
        DEFAULT_BLOCK_BREAKER_RADIUS("npc.defaults.block-breaker-radius", "npc.default.block-breaker-radius", -1),
        DEFAULT_CACHE_WAYPOINT_PATHS("npc.default.waypoints.cache-paths", false),
        DEFAULT_DESTINATION_TELEPORT_MARGIN("npc.pathfinding.defaults.destination-teleport-margin",
                "npc.pathfinding.default-destination-teleport-margin", -1),
        DEFAULT_DISTANCE_MARGIN("npc.pathfinding.default-distance-margin", 2),
        DEFAULT_LOOK_CLOSE("npc.default.look-close.enabled", false),
        DEFAULT_LOOK_CLOSE_RANGE("npc.default.look-close.range", 5),
        DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT("npc.hologram.default-line-height", 0.4D),
        DEFAULT_NPC_LIMIT("npc.limits.default-limit", 10),
        DEFAULT_PATH_DISTANCE_MARGIN("npc.pathfinding.default-path-distance-margin", 1),
        DEFAULT_PATHFINDER_UPDATE_PATH_RATE("npc.pathfinding.update-path-rate", "1s"),
        DEFAULT_PATHFINDING_RANGE("npc.default.pathfinding.range", "npc.pathfinding.default-range-blocks", 75F),
        DEFAULT_RANDOM_LOOK_CLOSE("npc.default.look-close.random-look-enabled", false),
        DEFAULT_RANDOM_LOOK_DELAY("npc.default.look-close.random-look-delay", "3s"),
        DEFAULT_RANDOM_TALKER("npc.default.random-talker", "npc.default.talk-close.random-talker", false),
        DEFAULT_REALISTIC_LOOKING("npc.default.realistic-looking", "npc.default.look-close.realistic-looking", false),
        DEFAULT_SPAWN_NODAMAGE_DURATION("npc.default.spawn-nodamage-ticks", "npc.default.spawn-nodamage-duration",
                "1s"),
        DEFAULT_STATIONARY_DURATION("npc.default.stationary-ticks", "npc.default.stationary-duration", -1),
        DEFAULT_STRAIGHT_LINE_TARGETING_DISTANCE("npc.pathfinding.straight-line-targeting-distance", 5),
        DEFAULT_TALK_CLOSE("npc.default.talk-close.enabled", false),
        DEFAULT_TALK_CLOSE_RANGE("npc.default.talk-close.range", 5),
        DEFAULT_TEXT("npc.default.talk-close.text.0", "Hi, I'm <npc>!") {
            @Override
            public void loadFromKey(DataKey root) {
                List<String> list = new ArrayList<String>();
                for (DataKey key : root.getRelative("npc.default.talk-close.text").getSubKeys()) {
                    list.add(key.getString(""));
                }
                value = list;
            }
        },
        DEFAULT_TEXT_DELAY_MAX("npc.text.default-random-text-delay-max", "10s"),
        DEFAULT_TEXT_DELAY_MIN("npc.text.default-random-text-delay-min", "5s"),
        DEFAULT_TEXT_SPEECH_BUBBLE_DURATION("npc.text.speech-bubble-ticks", "npc.text.speech-bubble-duration", "50t"),
        DISABLE_LOOKCLOSE_WHILE_NAVIGATING("npc.default.look-close.disable-while-navigating", true),
        DISABLE_MC_NAVIGATION_FALLBACK("npc.pathfinding.disable-mc-fallback-navigation", true),
        DISABLE_TABLIST("npc.tablist.disable", true),
        ENTITY_SPAWN_WAIT_DURATION("general.entity-spawn-wait-ticks", "general.wait-for-entity-spawn", "1s"),
        ERROR_COLOUR("general.color-scheme.message-error", "<red>"),
        FOLLOW_ACROSS_WORLDS("npc.follow.teleport-across-worlds", true),
        HIGHLIGHT_COLOUR("general.color-scheme.message-highlight", "yellow"),
        HOLOGRAM_UPDATE_RATE("npc.hologram.update-rate-ticks", "npc.hologram.update-rate", "1s"),
        INITIAL_PLAYER_JOIN_SKIN_PACKET_DELAY("npc.skins.player-join-update-delay-ticks",
                "npc.skins.player-join-update-delay", "3s"),
        KEEP_CHUNKS_LOADED("npc.chunks.always-keep-loaded", false),
        LOCALE("general.translation.locale", ""),
        MAX_CONTROLLABLE_GROUND_SPEED("npc.controllable.max-ground-speed", 0.5),
        MAX_NPC_LIMIT_CHECKS("npc.limits.max-permission-checks", 100),
        MAX_NPC_SKIN_RETRIES("npc.skins.max-retries", -1),
        MAX_PACKET_ENTRIES("npc.limits.max-packet-entries", 15),
        MAX_SPEED("npc.limits.max-speed", 100),
        MAX_TEXT_RANGE("npc.chat.options.max-text-range", 500),
        MAXIMUM_ASTAR_ITERATIONS("npc.pathfinding.maximum-new-pathfinder-iterations",
                "npc.pathfinding.new-finder.maximum-iterations", 5000),
        MAXIMUM_VISITED_NODES("npc.pathfinding.maximum-visited-nodes", "npc.pathfinding.maximum-visited-blocks", 768),
        MESSAGE_COLOUR("general.color-scheme.message", "<green>"),
        NEW_PATHFINDER_CHECK_BOUNDING_BOXES("npc.pathfinding.new-finder.check-bounding-boxes", false),
        NEW_PATHFINDER_OPENS_DOORS("npc.pathfinding.new-finder.open-doors", false),
        NPC_ATTACK_DISTANCE("npc.pathfinding.attack-range", 1.75),
        NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN("npc.commands.global-delay-seconds", "npc.commands.global-cooldown", "1s"),
        NPC_COMMAND_MAXIMUM_TIMES_USED_MESSAGE("npc.commands.error-messages.maximum-times-used",
                "You have reached the maximum number of uses ({0})."),
        NPC_COMMAND_MISSING_ITEM_MESSAGE("npc.commands.error-messages.missing-item", "Missing {1} {0}"),
        NPC_COMMAND_NO_PERMISSION_MESSAGE("npc.commands.error-messages.no-permission",
                "You don't have permission to do that."),
        NPC_COMMAND_NOT_ENOUGH_EXPERIENCE_MESSAGE("npc.commands.error-messages.not-enough-experience",
                "You need at least {0} experience."),
        NPC_COMMAND_NOT_ENOUGH_MONEY_MESSAGE("npc.commands.error-messages.not-enough-money", "You need at least ${0}."),
        NPC_COMMAND_ON_COOLDOWN_MESSAGE("npc.commands.error-messages.on-cooldown",
                "Please wait for {minutes} minutes and {seconds_over} seconds."),
        NPC_COMMAND_ON_GLOBAL_COOLDOWN_MESSAGE("npc.commands.error-messages.on-global-cooldown",
                "Please wait for {minutes} minutes and {seconds_over} seconds."),
        NPC_COST("economy.npc.cost", 100D),
        NPC_SKIN_RETRY_DELAY("npc.skins.retry-delay", "5s"),
        NPC_SKIN_ROTATION_UPDATE_DEGREES("npc.skins.rotation-update-degrees", 90f),
        NPC_SKIN_USE_LATEST("npc.skins.use-latest-by-default", false),
        NPC_SKIN_VIEW_DISTANCE("npc.skins.view-distance", 100D),
        NPC_WATER_SPEED_MODIFIER("npc.movement.water-speed-modifier", 1.15F),
        PACKET_UPDATE_DELAY("npc.packets.update-delay", 30),
        PATHFINDER_FALL_DISTANCE("npc.pathfinding.allowed-fall-distance", -1),
        PLACEHOLDER_SKIN_UPDATE_FREQUENCY("npc.skins.placeholder-update-frequency-ticks",
                "npc.skins.placeholder-update-frequency", "5m"),
        PLAYER_TELEPORT_DELAY("npc.delay-player-teleport-ticks", "npc.delay-player-teleport", -1),
        REMOVE_PLAYERS_FROM_PLAYER_LIST("npc.player.remove-from-list", true),
        RESOURCE_PACK_PATH("general.resource-pack-path", "plugins/Citizens/resourcepack"),
        SAVE_TASK_DELAY("storage.save-task.delay", "1hr"),
        SELECTION_ITEM("npc.selection.item", "stick"),
        SELECTION_MESSAGE("npc.selection.message", "Selected [[<npc>]] (ID [[<id>]])."),
        SERVER_OWNS_NPCS("npc.server-ownership", false),
        STORAGE_FILE("storage.file", "saves.yml"),
        STORAGE_TYPE("storage.type", "yaml"),
        SUBPLUGIN_FOLDER("subplugins.folder", "plugins"),
        TABLIST_REMOVE_PACKET_DELAY("npc.tablist.remove-packet-delay", "1t"),
        TALK_CLOSE_TO_NPCS("npc.chat.options.talk-to-npcs", true),
        TALK_ITEM("npc.text.talk-item", "*"),
        USE_BOAT_CONTROLS("npc.controllable.use-boat-controls", true),
        USE_NEW_PATHFINDER("npc.pathfinding.use-new-finder", false),
        USE_SCOREBOARD_TEAMS("npc.scoreboard-teams.enable", true),
        WARN_ON_RELOAD("general.reload-warning-enabled", true);

        private Duration duration;
        protected String migrate;
        protected String path;
        protected Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        Setting(String migrate, String path, Object value) {
            this.migrate = migrate;
            this.path = path;
            this.value = value;
        }

        public boolean asBoolean() {
            return (Boolean) value;
        }

        public double asDouble() {
            return ((Number) value).doubleValue();
        }

        public float asFloat() {
            return ((Number) value).floatValue();
        }

        public int asInt() {
            if (value instanceof String) {
                return Integer.parseInt(value.toString());
            }
            return ((Number) value).intValue();
        }

        @SuppressWarnings("unchecked")
        public List<String> asList() {
            if (!(value instanceof List)) {
                value = Lists.newArrayList(value);
            }
            return (List<String>) value;
        }

        public int asSeconds() {
            if (duration == null) {
                duration = SpigotUtil.parseDuration(asString());
            }
            return Util.convert(TimeUnit.SECONDS, duration);
        }

        public String asString() {
            return value.toString();
        }

        public int asTicks() {
            if (duration == null) {
                duration = SpigotUtil.parseDuration(asString());
            }
            return Util.toTicks(duration);
        }

        protected void loadFromKey(DataKey root) {
            if (migrate != null && root.keyExists(migrate) && !root.keyExists(path)) {
                value = root.getRaw(migrate);
            } else {
                value = root.getRaw(path);
            }
        }

        protected void setAtKey(DataKey root) {
            root.setRaw(path, value);
        }
    }
}
