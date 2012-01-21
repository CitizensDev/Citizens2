package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.storage.flatfile.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    public enum Setting {
        DEBUG_MODE("general.debug-mode", false),
        TEST_DOUBLE("hi", 3.4);

        private String path;
        private Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public int getInt() {
            return (Integer) value;
        }

        public double getDouble() {
            return (Double) value;
        }

        public long getLong() {
            return (Long) value;
        }

        public boolean getBoolean() {
            return (Boolean) value;
        }

        public String getString() {
            return value.toString();
        }

        private Object get() {
            return value;
        }

        private void set(Object value) {
            this.value = value;
        }
    }

    private YamlStorage config;
    private final DataKey root;

    public Settings(Citizens plugin) {
        config = new YamlStorage(plugin.getDataFolder() + File.separator + "config.yml");
        root = config.getKey("");
    }

    public void load() {
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
}