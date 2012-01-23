package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.storage.flatfile.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    private final YamlStorage config;

    public Settings(Citizens plugin) {
        config = new YamlStorage(plugin.getDataFolder() + File.separator + "config.yml");
    }

    public void load() {
        DataKey root = config.getKey("");
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.getPath())) {
                Messaging.log("Writing default setting: '" + setting.getPath() + "'");
                root.setRaw(setting.getPath(), setting.get());
            } else
                setting.set(root.getRaw(setting.getPath()));
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
        SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!");

        private String path;
        private Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        private Object get() {
            return value;
        }

        public boolean getBoolean() {
            return (Boolean) value;
        }

        public double getDouble() {
            return (Double) value;
        }

        public int getInt() {
            return (Integer) value;
        }

        public long getLong() {
            return (Long) value;
        }

        public String getPath() {
            return path;
        }

        public String getString() {
            return value.toString();
        }

        private void set(Object value) {
            this.value = value;
        }
    }
}