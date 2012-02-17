package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    private final YamlStorage config;

    public Settings(Citizens plugin) {
        config = new YamlStorage(plugin.getDataFolder() + File.separator + "config.yml", "Citizens Configuration");
    }

    public void load() {
        DataKey root = config.getKey("");
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                Messaging.log("Writing default setting: '" + setting.path + "'");
                root.setRaw(setting.path, setting.get());
            } else
                setting.set(root.getRaw(setting.path));
        }
        save();
    }

    public void save() {
        config.save();
    }

    public enum Setting {
        CHAT_PREFIX("npc.chat.prefix", "[<npc>]: "),
        DEBUG_MODE("general.debug-mode", false),
        QUICK_SELECT("npc.selection.quick-select", false),
        SELECTION_ITEM("npc.selection.item", 280),
        SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!"),
        USE_DATABASE("use-database", false),
        DATABASE_PASSWORD("database.password", ""),
        DATABASE_USERNAME("database.username", ""),
        DATABASE_URL("database.url", ""),
        DATABASE_DRIVER("database.driver", "");

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
            return (Integer) value;
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