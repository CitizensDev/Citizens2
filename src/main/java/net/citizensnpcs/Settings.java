package net.citizensnpcs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.Messaging;

public class Settings {
    private final Storage config;
    private final DataKey root;

    public Settings(File folder) {
        config = new YamlStorage(folder + File.separator + "config.yml", "Citizens Configuration");
        root = config.getKey("");

        config.load();
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                Messaging.logF("Writing default setting: '%s'", setting.path);
                setting.set(root);
            } else
                setting.load(root);
        }

        save();
    }

    public void reload() {
        config.load();
        for (Setting setting : Setting.values())
            if (root.keyExists(setting.path))
                setting.load(root);

        save();
    }

    public void save() {
        config.save();
    }

    public enum Setting {
        CHAT_PREFIX("npc.chat.prefix", "[<npc>]: "),
        DATABASE_DRIVER("storage.database.driver", ""),
        DATABASE_PASSWORD("storage.database.password", ""),
        DATABASE_URL("storage.database.url", ""),
        DATABASE_USERNAME("storage.database.username", ""),
        DEBUG_MODE("general.debug-mode", false),
        DEFAULT_LOOK_CLOSE("npc.default.look-close", false),
        DEFAULT_RANDOM_TALKER("npc.default.random-talker", true),
        DEFAULT_TALK_CLOSE("npc.default.talk-close", false),
        DEFAULT_TEXT("npc.default.text.0", "Hi, I'm <npc>!") {
            @Override
            public void load(DataKey root) {
                List<String> list = new ArrayList<String>();
                for (DataKey key : root.getRelative("npc.default.text").getSubKeys())
                    list.add(key.getString(""));
                value = list;
            }
        },
        QUICK_SELECT("npc.selection.quick-select", false),
        SELECTION_ITEM("npc.selection.item", "280"),
        SELECTION_MESSAGE("npc.selection.message", "<b>You selected <a><npc><b>!"),
        SERVER_OWNS_NPCS("npc.server-ownership", false),
        STORAGE_FILE("storage.file", "saves.yml"),
        STORAGE_TYPE("storage.type", "yaml"),
        TALK_CLOSE_MAXIMUM_COOLDOWN("npc.text.max-talk-cooldown", 60),
        TALK_CLOSE_MINIMUM_COOLDOWN("npc.text.min-talk-cooldown", 30),
        TALK_ITEM("npc.text.talk-item", "340");

        protected String path;
        protected Object value;

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

        @SuppressWarnings("unchecked")
        public List<String> asList() {
            return (List<String>) value;
        }

        public long asLong() {
            return (Long) value;
        }

        public String asString() {
            return value.toString();
        }

        protected void set(DataKey root) {
            root.setRaw(path, value);
        }
        
        protected void load(DataKey root) {
            value = root.getRaw(path);
        }
    }
}