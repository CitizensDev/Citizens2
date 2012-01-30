package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.storage.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    private final YamlStorage config;

    public Settings(Citizens plugin) {
        config = new YamlStorage(plugin.getDataFolder() + File.separator + "config.yml");
    }

    public void load() {
        DataKey root = config.getKey("");
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                Messaging.log("Writing default setting: '" + setting.path + "'");
                root.setRaw(setting.path, setting.get());
            } else {
                setting.set(root.getRaw(setting.path));
            }
        }
        save();
    }

    public void save() {
        config.save();
    }

    public enum Setting {
        DEBUG_MODE("general.debug-mode", false),
        USE_DATABASE("use-database", false),
        SELECTION_ITEM("npc.selection.item", 280),
        SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!"),
        QUICK_SELECT("npc.selection.quick-select", false);

        private String path;
        private Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        private Object get() {
            return value;
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

        private void set(Object value) {
            this.value = value;
        }
    }
}