package net.citizensnpcs;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.NMS;
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
        Messaging.configure(file, Setting.DEBUG_MODE.asBoolean(), Setting.RESET_FORMATTING_ON_COLOR_CHANGE.asBoolean(),
                Setting.MESSAGE_COLOUR.asString(), Setting.HIGHLIGHT_COLOUR.asString(), Setting.ERROR_COLOUR.asString(),
                NMS::sendComponent);
    }

    public enum Setting {
        ALWAYS_USE_NAME_HOLOGRAM("Always use holograms for names instead of only for hex colors / placeholders",
                "npc.always-use-name-holograms", false),
        ASTAR_ITERATIONS_PER_TICK("Number of blocks to search per tick (Citizens pathfinder)",
                "npc.pathfinding.new-finder.iterations-per-tick", "npc.pathfinding.new-finder.iterations-per-tick",
                250),
        AUTH_SERVER_URL("Search for gameprofiles using this URL", "general.authlib.profile-url",
                "https://sessionserver.mojang.com/session/minecraft/profile/"),
        BOSSBAR_RANGE("The default bossbar range, in blocks", "npc.default.bossbar-view-range", 64),
        CHAT_BYSTANDERS_HEAR_TARGETED_CHAT(
                "Whether nearby players also hear text, even if targeted at a specific player",
                "npc.chat.options.bystanders-hear-targeted-chat", false),
        CHAT_FORMAT("The default text format (placeholder enabled)", "npc.chat.format.no-targets", "[<npc>]: <text>"),
        CHAT_FORMAT_TO_BYSTANDERS("The default text format for nearby players (placeholder enabled)",
                "npc.chat.format.with-target-to-bystanders", "[<npc>] -> [<target>]: <text>"),
        CHAT_FORMAT_TO_TARGET("The default text format for targeted text (placeholder enabled)",
                "npc.chat.format.to-target", "<npc>: <text>"),
        CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS("The default text format for nearby players (placeholder enabled)",
                "npc.chat.format.with-targets-to-bystanders", "[<npc>] -> [<targets>]: <text>"),
        CHAT_MAX_NUMBER_OF_TARGETS("Number of target names to show to bystanders",
                "npc.chat.options.max-number-of-targets-to-show", 2),
        CHAT_MULTIPLE_TARGETS_FORMAT("npc.chat.options.multiple-targets-format",
                "<target>|, <target>| & <target>| & others"),
        CHAT_RANGE("Nearby player range in blocks", "npc.chat.options.range", 5),
        CHECK_MINECRAFT_VERSION("Whether to check the minecraft version for compatibility (do not change)",
                "advanced.check-minecraft-version", true),
        CONTROLLABLE_GROUND_DIRECTION_MODIFIER("The percentage to increase speed when controlling NPCs on the ground",
                "npc.controllable.ground-direction-modifier", 1.0D),
        DEBUG_CHUNK_LOADS("Debug chunk load stack traces, not as useful in recent Minecraft versions",
                "general.debug-chunk-loads", false),
        DEBUG_FILE("Send Citizens debug output to a specific file", "general.debug-file", ""),
        DEBUG_MODE("Enable Citizens debugging", "general.debug-mode", false),
        DEBUG_PATHFINDING("Debug pathfinding by showing fake target blocks", "npc.pathfinding.debug",
                "npc.pathfinding.debug-paths", false),
        DEFAULT_BLOCK_BREAKER_RADIUS(
                "The default distance radius for block breaking, in blocks<br>The NPC will pathfind to be this far away from the target block if greater than 0",
                "npc.defaults.block-breaker-radius", "npc.default.block-breaker-radius", -1),
        DEFAULT_CACHE_WAYPOINT_PATHS(
                "Whether to cache /npc path by default<br>Can eliminate pathfinding for repetitive static paths",
                "npc.default.waypoints.cache-paths", false),
        DEFAULT_DESTINATION_TELEPORT_MARGIN(
                "The default distance in blocks where the NPC will just teleport to the destination<br>Useful when trying to get exactly to the destination",
                "npc.pathfinding.defaults.destination-teleport-margin",
                "npc.pathfinding.default-destination-teleport-margin", -1),
        DEFAULT_DISTANCE_MARGIN(
                "The default MOVEMENT distance in blocks where the NPC will move to before considering a path finished<br>Note: this is different from the PATHFINDING distance which is specified by path-distance-margin",
                "npc.pathfinding.default-distance-margin", 1),
        DEFAULT_HOLOGRAM_BACKGROUND_COLOR(
                "The default background color for holograms, specified as an RGB or RGBA value<br>For example 0,255,123 would be green and 255,255,255,255 would be transparent",
                "npc.hologram.default-background-color", ""),
        DEFAULT_HOLOGRAM_RENDERER(
                "The default renderer for holograms, must be one of the following:<br>interaction - requires 1.19+, matches nametags more closely than display<br>display - allows for different colored backgrounds<br>display_vehicle - mounts the display on the NPC<br>areaeffectcloud - the safest option<br>armorstand - the second safest option, has a hitbox clientside<br>armorstand_vehicle - mounts the armorstand on the NPC, only useful for nameplates",
                "npc.hologram.default-renderer", "display"),
        DEFAULT_LOOK_CLOSE("Enable look close by default", "npc.default.look-close.enabled", false),
        DEFAULT_LOOK_CLOSE_RANGE("Default look close range in blocks", "npc.default.look-close.range", 10),
        DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT("Default distance between hologram lines", "npc.hologram.default-line-height",
                0.4D),
        DEFAULT_NPC_LIMIT(
                "Default maximum number of NPCs owned by a single player (give the citizens ignore-limits permission to skip this check)",
                "npc.limits.default-limit", 10),
        DEFAULT_PATH_DISTANCE_MARGIN(
                "Default PATHFINDING distance in blocks where the NPC will consider pathfinding complete<br>Note: this is different from the MOVEMENT distance, which is specified by the distance-margin<br>Set to 0 if you want to try pathfind exactly to the target destination",
                "npc.pathfinding.default-path-distance-margin", 1),
        DEFAULT_PATHFINDER_UPDATE_PATH_RATE("How often to repathfind when targeting a dynamic target such as an entity",
                "npc.pathfinding.update-path-rate", "1s"),
        DEFAULT_PATHFINDING_RANGE(
                "The default pathfinding range in blocks<br>Shouldn't be set too high to avoid lag - try pathfinding in shorter segments instead",
                "npc.default.pathfinding.range", "npc.pathfinding.default-range-blocks", 75F),
        DEFAULT_RANDOM_LOOK_CLOSE("Default random look close enabled", "npc.default.look-close.random-look-enabled",
                false),
        DEFAULT_RANDOM_LOOK_DELAY("Default random look delay", "npc.default.look-close.random-look-delay", "3s"),
        DEFAULT_RANDOM_TALKER("Default talk to nearby players", "npc.default.random-talker",
                "npc.default.talk-close.random-talker", false),
        DEFAULT_REALISTIC_LOOKING("Default to checking for line of sight when looking at players",
                "npc.default.realistic-looking", "npc.default.look-close.realistic-looking", false),
        DEFAULT_SPAWN_NODAMAGE_DURATION(
                "Default duration of invincibility on entity spawn, Minecraft default is 20 ticks",
                "npc.default.spawn-nodamage-duration", "npc.default.spawn-invincibility-duration", "1s"),
        DEFAULT_STATIONARY_DURATION(
                "Default duration in the same location before the NPC considers itself stuck and failed pathfinding",
                "npc.default.stationary-duration", "npc.pathfinding.default-stationary-duration", -1),
        DEFAULT_STRAIGHT_LINE_TARGETING_DISTANCE(
                "The distance in blocks where the NPC will switch to walking straight towards the target instead of pathfinding<br>Currently only for dynamic targets like entities",
                "npc.pathfinding.straight-line-targeting-distance", 5),
        DEFAULT_STUCK_ACTION(
                "The default action to perform when NPCs are unable to find a path or are stuck in the same block for too long<br>Supported options are: 'teleport to destination' or 'none'",
                "npc.pathfinding.default-stuck-action", "none"),
        DEFAULT_TALK_CLOSE("npc.default.talk-close.enabled", false),
        DEFAULT_TALK_CLOSE_RANGE("Default talk close range in blocks", "npc.default.talk-close.range", 5),
        DEFAULT_TEXT("npc.default.talk-close.text", "Hi, I'm <npc>!") {
            @SuppressWarnings("unchecked")
            @Override
            public void loadFromKey(DataKey root) {
                List<String> list = new ArrayList<>();
                Object raw = root.getRaw(path);
                if (raw instanceof ConfigurationSection || raw instanceof Map) {
                    for (DataKey key : root.getRelative(path).getSubKeys()) {
                        list.add(key.getString(""));
                    }
                } else if (raw instanceof Collection) {
                    list.addAll((Collection<? extends String>) raw);
                }
                value = list;
            }

            @Override
            protected void setAtKey(DataKey root) {
                root.setRaw(path, Lists.newArrayList(value));
                setComments(root);
            }
        },
        DEFAULT_TEXT_DELAY_MAX("Default maximum delay when talking to players",
                "npc.text.default-random-text-delay-max", "10s"),
        DEFAULT_TEXT_DELAY_MIN("Default minimum delay when talking to players",
                "npc.text.default-random-text-delay-min", "5s"),
        DEFAULT_TEXT_SPEECH_BUBBLE_DURATION("Default duration that speech bubbles show up for",
                "npc.text.speech-bubble-ticks", "npc.text.speech-bubble-duration", "50t"),
        DISABLE_LOOKCLOSE_WHILE_NAVIGATING("Whether to disable look close while pathfinding",
                "npc.default.look-close.disable-while-navigating", true),
        DISABLE_MC_NAVIGATION_FALLBACK(
                "Minecraft will pick a 'close-enough' location when pathfinding to a block if it can't find a direct path<br>Disabled by default",
                "npc.pathfinding.disable-mc-fallback-navigation",
                "npc.pathfinding.minecraft.disable-fallback-navigation", true),
        DISABLE_TABLIST("Whether to remove NPCs from the tablist", "npc.tablist.disable", true),
        ENTITY_SPAWN_WAIT_DURATION(
                "Entities are no longer spawned until the chunks are loaded from disk<br>Wait for chunk loading for one second by default, increase if your disk is slow",
                "general.entity-spawn-wait-ticks", "general.wait-for-entity-spawn", "1s"),
        ERROR_COLOUR("general.color-scheme.message-error", "<red>"),
        EXPERIMENTAL_LIST_STORAGE(
                "Whether to store lists as YAML lists in save files.<br>Experimental due to risk of data loss but makes the YAML files easier to edit.",
                "general.experimental-list-storage", false),
        FOLLOW_ACROSS_WORLDS("Whether /npc follow will teleport across worlds to follow its target",
                "npc.follow.teleport-across-worlds", false),
        HIGHLIGHT_COLOUR("general.color-scheme.message-highlight", "<yellow>"),
        HOLOGRAM_ALWAYS_UPDATE_POSITION("Whether to always update the hologram position every tick",
                "npc.hologram.always-update-position", false),
        HOLOGRAM_UPDATE_RATE("How often to update hologram names (including placeholders)",
                "npc.hologram.update-rate-ticks", "npc.hologram.update-rate", "1s"),
        HOOK_PROTOCOLLIB("Whether to hook into ProtocolLib", "general.interop.protocollib", true),
        INITIAL_PLAYER_JOIN_SKIN_PACKET_DELAY("How long to wait before sending skins to joined players",
                "npc.skins.player-join-update-delay-ticks", "npc.skins.player-join-update-delay", "1s"),
        KEEP_CHUNKS_LOADED("Whether to keep NPC chunks loaded", "npc.chunks.always-keep-loaded", false),
        LOCALE("Controls translation files - defaults to your system language, set to 'en' if English required",
                "general.translation.locale", ""),
        MAX_CONTROLLABLE_FLIGHT_SPEED(
                "The maximum flying speed that controllable NPCs can reach, in Minecraft velocity units",
                "npc.controllable.max-flying-speed", 0.75),
        MAX_CONTROLLABLE_GROUND_SPEED("The maximum speed that controllable NPCs can reach, in Minecraft velocity units",
                "npc.controllable.max-ground-speed", 0.5),
        MAX_NPC_LIMIT_CHECKS(
                "How many permissions to check when creating NPCs<br>Only change if you have a permission limit greater than this",
                "npc.limits.max-permission-checks", 100),
        MAX_NPC_SKIN_RETRIES(
                "How many times to try load NPC skins (due to Minecraft rate-limiting skin requests, should rarely be less than 5",
                "npc.skins.max-retries", -1),
        MAXIMUM_ASTAR_ITERATIONS("The maximum number of blocks to check when pathfinding",
                "npc.pathfinding.maximum-new-pathfinder-iterations", "npc.pathfinding.new-finder.maximum-iterations",
                1024),
        MAXIMUM_VISITED_NODES("The maximum number of blocks to check", "npc.pathfinding.maximum-visited-blocks",
                "npc.pathfinding.minecraft.max-visited-blocks", 1024),
        MESSAGE_COLOUR("general.color-scheme.message", "<green>"),
        NEW_PATHFINDER_CHECK_BOUNDING_BOXES(
                "Whether to check bounding boxes when pathfinding such as between fences, inside doors, or other half-blocks",
                "npc.pathfinding.new-finder.check-bounding-boxes", false),
        NEW_PATHFINDER_OPENS_DOORS("Whether to open doors while pathfinding (should close them as well)",
                "npc.pathfinding.new-finder.open-doors", false),
        NPC_ATTACK_DISTANCE("The range in blocks before attacking the target", "npc.pathfinding.attack-range", 1.75),
        NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN(
                "The global cooldown before a command can be used again, must be in seconds",
                "npc.commands.global-delay-seconds", "npc.commands.global-cooldown", "1s"),
        NPC_COMMAND_GLOBAL_MAXIMUM_TIMES_USED_MESSAGE("npc.commands.error-messages.global-maximum-times-used",
                "You have reached the maximum number of uses ({0})."),
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
        NPC_COST("The default cost to create an NPC", "npc.defaults.npc-cost", "npc.default.npc-cost", 100D),
        NPC_SKIN_FETCH_DEFAULT(
                "Whether to try and look for the player skin for all new NPCs<br>If this is set to false and you create an NPC named Dinnerbone, the NPC will have the default (steve/alex/etc) skin rather than trying to fetch the Dinnerbone skin",
                "npc.skins.try-fetch-default-skin", true),
        NPC_SKIN_RETRY_DELAY("How long before retrying skin requests (typically due to Mojang rate limiting)",
                "npc.skins.retry-delay", "5s"),
        NPC_SKIN_ROTATION_UPDATE_DEGREES("npc.skins.rotation-update-degrees", 90f),
        NPC_SKIN_USE_LATEST("Whether to fetch new skins from Minecraft every so often",
                "npc.skins.use-latest-by-default", false),
        NPC_SKIN_VIEW_DISTANCE("View distance in blocks", "npc.skins.view-distance", 100),
        NPC_WATER_SPEED_MODIFIER("Movement speed percentage increase while in water",
                "npc.movement.water-speed-modifier", 1.15F),
        PACKET_HOLOGRAMS("Use packet NPCs for name holograms (experimental)", "npc.use-packet-holograms", false),
        PACKET_UPDATE_DELAY("npc.packets.update-delay", 30),
        PATHFINDER_FALL_DISTANCE(
                "The default allowed maximum fall distance when pathfinding, set to -1 to use the default value",
                "npc.pathfinding.allowed-fall-distance", -1),
        PATHFINDER_TYPE("The pathfinder type.<br>Valid options are: CITIZENS or MINECRAFT.",
                "npc.pathfinding.pathfinder-type", "MINECRAFT"),
        PLACEHOLDER_SKIN_UPDATE_FREQUENCY("How often to update skin placeholders",
                "npc.skins.placeholder-update-frequency-ticks", "npc.skins.placeholder-update-frequency", "5m"),
        PLAYER_TELEPORT_DELAY("npc.delay-player-teleport-ticks", "npc.delay-player-teleport", -1),
        REMOVE_PLAYERS_FROM_PLAYER_LIST("Whether to remove NPCs from the Java list of players",
                "npc.player.remove-from-list", true),
        RESET_FORMATTING_ON_COLOR_CHANGE(
                "Whether to reset formatting on color change.<br>Emulates old color code behavior.",
                "general.reset-formatting-on-color-change", false),
        RESET_YAW_ON_SPAWN(
                "Whether to reset NPC yaw on spawn<br>Currently this is implemented by an arm swing animation due to Minecraft limitations",
                "npc.default.reset-yaw-on-spawn", true),
        RESOURCE_PACK_PATH("The resource pack path to save resource packs to", "general.resource-pack-path",
                "plugins/Citizens/resourcepack"),
        SAVE_TASK_FREQUENCY("How often to save NPCs to disk", "storage.save-task.delay", "storage.save-task-frequency",
                "1hr"),
        SELECTION_ITEM("The default item in hand to select an NPC", "npc.selection.item", "stick"),
        SELECTION_MESSAGE("npc.selection.message", "Selected [[<npc>]] (ID [[<id>]])."),
        SERVER_OWNS_NPCS("Whether the server owns NPCs rather than individual players", "npc.server-ownership", false),
        SHOP_DEFAULT_ITEM_SETTINGS("npc.shops.default-item",
                Maps.asMap(
                        Sets.newHashSet("times-purchasable", "max-repeats-on-shift-click", "result-message", "name",
                                "cost-message", "lore", "already-purchased-message", "click-to-confirm-message"),
                        s -> "")),
        SHOP_GLOBAL_VIEW_PERMISSION(
                "The global view permission that players need to view any NPC shop<br>Defaults to empty (no permission required).",
                "npc.defaults.shops.global-view-permission", "npc.shops.global-view-permission", ""),
        STORAGE_FILE("storage.file", "saves.yml"),
        TABLIST_REMOVE_PACKET_DELAY("How long to wait before sending the tablist remove packet",
                "npc.tablist.remove-packet-delay", "2t"),
        TALK_CLOSE_TO_NPCS("Whether to talk to NPCs (and therefore bystanders) as well as players",
                "npc.chat.options.talk-to-npcs", true),
        TALK_ITEM("The item filter to talk with", "npc.text.talk-item", "*"),
        USE_BOAT_CONTROLS("Whether to change vehicle direction with movement instead of strafe controls",
                "npc.controllable.use-boat-controls", true),
        USE_SCOREBOARD_TEAMS("npc.defaults.enable-scoreboard-teams", "npc.default.enable-scoreboard-teams", true),
        WARN_ON_RELOAD("general.reload-warning-enabled", true);

        private String comments;
        private Duration duration;
        private String migrateFrom;
        protected final String path;
        protected Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        Setting(String migrateOrComments, String path, Object value) {
            if (migrateOrComments.contains(".") && !migrateOrComments.contains(" ")) {
                migrateFrom = migrateOrComments;
            } else {
                comments = migrateOrComments;
            }
            this.path = path;
            this.value = value;
        }

        Setting(String comments, String migrate, String path, Object value) {
            this.migrateFrom = migrate;
            this.comments = comments;
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
            if (value instanceof String)
                return Integer.parseInt(value.toString());
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
                duration = SpigotUtil.parseDuration(asString(), null);
            }
            return Util.convert(TimeUnit.SECONDS, duration);
        }

        public ConfigurationSection asSection() {
            if (!(value instanceof ConfigurationSection))
                return new MemoryConfiguration();

            return (ConfigurationSection) value;
        }

        public String asString() {
            return value.toString();
        }

        public int asTicks() {
            if (duration == null) {
                duration = SpigotUtil.parseDuration(asString(), null);
            }
            return Util.toTicks(duration);
        }

        protected void loadFromKey(DataKey root) {
            setComments(root);
            if (migrateFrom != null && root.keyExists(migrateFrom) && !root.keyExists(path)) {
                value = root.getRaw(migrateFrom);
                root.removeKey(migrateFrom);
            } else {
                value = root.getRaw(path);
            }
        }

        protected void setAtKey(DataKey root) {
            root.setRaw(path, value);
            setComments(root);
        }

        protected void setComments(DataKey root) {
            if (!SUPPORTS_SET_COMMENTS || !root.keyExists(path))
                return;
            ((MemoryDataKey) root).getSection("").setComments(path,
                    comments == null ? null : Arrays.asList(comments.split("<br>")));
        }
    }

    private static boolean SUPPORTS_SET_COMMENTS = true;
    static {
        try {
            ConfigurationSection.class.getMethod("getInlineComments", String.class);
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORTS_SET_COMMENTS = false;
        }
    }
}
