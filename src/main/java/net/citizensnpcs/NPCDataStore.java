package net.citizensnpcs;

import java.io.File;
import java.sql.SQLException;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.util.Messaging;

import org.bukkit.entity.EntityType;

public class NPCDataStore {
    private final Storage root;

    private NPCDataStore(Storage saves) {
        root = saves;
    }

    public void loadInto(CitizensNPCRegistry registry) {
        int created = 0, spawned = 0;
        for (DataKey key : root.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name")) {
                Messaging.logF("Could not find a name for the NPC with ID '%s'.", id);
                continue;
            }
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = EntityType.fromName(unparsedEntityType);
            if (type == null) {
                try {
                    type = EntityType.valueOf(unparsedEntityType);
                } catch (IllegalArgumentException ex) {
                    Messaging.logF("NPC type '%s' was not recognized. Did you spell it correctly?",
                            unparsedEntityType);
                    continue;
                }
            }
            NPC npc = registry.createNPC(type, id, key.getString("name"));
            ((CitizensNPC) npc).load(key);

            ++created;
            if (npc.isSpawned())
                ++spawned;
        }
        Messaging.logF("Loaded %d NPCs (%d spawned).", created, spawned);
    }

    public void remove(NPC npc) {
        root.getKey("npc").removeKey(Integer.toString(npc.getId()));
    }

    public void saveToDisk() {
        new Thread() {
            @Override
            public void run() {
                root.save();
            }
        }.start();
    }

    public void saveToDiskImmediate() {
        root.save();
    }

    public void store(NPC npc) {
        ((CitizensNPC) npc).save(root.getKey("npc." + npc.getId()));
    }

    public static NPCDataStore create(File folder) {
        Storage saves = null;
        String type = Setting.STORAGE_TYPE.asString();
        if (type.equalsIgnoreCase("db") || type.equalsIgnoreCase("database")) {
            try {
                saves = new DatabaseStorage(Setting.DATABASE_DRIVER.asString(),
                        Setting.DATABASE_URL.asString(), Setting.DATABASE_USERNAME.asString(),
                        Setting.DATABASE_PASSWORD.asString());
            } catch (SQLException e) {
                e.printStackTrace();
                Messaging.log("Unable to connect to database, falling back to YAML");
            }
        } else if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(folder + File.separator + Setting.STORAGE_FILE.asString(),
                    "Citizens NPC Storage");
        }
        if (saves == null)
            saves = new YamlStorage(new File(folder, Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
        if (!saves.load())
            return null;
        Messaging.logF("Save method set to %s.", saves.toString());
        return new NPCDataStore(saves);
    }
}
