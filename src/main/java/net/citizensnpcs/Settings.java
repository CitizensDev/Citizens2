package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    private final Storage config;

    public Settings(File folder) {
        config = new YamlStorage(folder + File.separator + "config.yml", "Citizens Configuration");
    }

    public void load() {
        config.load();
        DataKey root = config.getKey("");
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                Messaging.log("Writing default setting: '" + setting.path + "'");
                root.setRaw(setting.path, setting.get());
            } else
                setting.set(root.getRaw(setting.path));
        }
    }

    public void save() {
        config.save();
    }

    public enum Setting {
        CHAT_PREFIX("npc.chat.prefix", "[<npc>]: "),
        DATABASE_DRIVER("database.driver", ""),
        DATABASE_PASSWORD("database.password", ""),
        DATABASE_URL("database.url", ""),
        DATABASE_USERNAME("database.username", ""),
        DEBUG_MODE("general.debug-mode", false),
        DEFAULT_LOOK_CLOSE("npc.default.look-close", false),
        DEFAULT_RANDOM_TALKER("npc.default.random-talker", true),
        DEFAULT_TALK_CLOSE("npc.default.talk-close", false),
        QUICK_SELECT("npc.selection.quick-select", false),
        SELECTION_ITEM("npc.selection.item", "280"),
        SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!"),
        TALK_CLOSE_MAXIMUM_COOLDOWN("npc.text.max-talk-cooldown", 60),
        TALK_CLOSE_MINIMUM_COOLDOWN("npc.text.min-talk-cooldown", 30),
        TALK_ITEM("npc.text.talk-item", "340"),
        USE_DATABASE("use-database", false);

        private String path;
        private Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public boolean asBoolean() {
            return (Boolean) value;
        }

        public double asDouble() {
            return (Double) value;
        }

        public int asInt() {
            return Integer.parseInt(value.toString());
        }

        public long asLong() {
            return (Long) value;
        }

        public String asString() {
            return value.toString();
        }

        private Object get() {
            return value;
        }

        private void set(Object value) {
            this.value = value;
        }
    }
}