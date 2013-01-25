package net.citizensnpcs;

import java.io.File;
import java.sql.SQLException;

import net.citizensnpcs.Metrics.Graph;
import net.citizensnpcs.Metrics.Plotter;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

import org.bukkit.entity.EntityType;

public class NPCDataStore {
    private final Storage root;

    private NPCDataStore(Storage saves) {
        root = saves;
    }

    public void addPlotters(Graph graph) {
        graph.addPlotter(new Plotter("Database") {
            @Override
            public int getValue() {
                return root instanceof DatabaseStorage ? 1 : 0;
            }
        });
        graph.addPlotter(new Plotter("YAML") {
            @Override
            public int getValue() {
                return root instanceof YamlStorage ? 1 : 0;
            }
        });
        graph.addPlotter(new Plotter("NBT") {
            @Override
            public int getValue() {
                return root instanceof NBTStorage ? 1 : 0;
            }
        });
    }

    public void loadInto(CitizensNPCRegistry registry) {
        int created = 0;
        for (DataKey key : root.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name")) {
                Messaging.logTr(Messages.LOAD_NAME_NOT_FOUND, id);
                continue;
            }
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = Util.matchEntityType(unparsedEntityType);
            if (type == null) {
                Messaging.logTr(Messages.LOAD_UNKNOWN_NPC_TYPE, unparsedEntityType);
                continue;
            }
            NPC npc = registry.createNPC(type, id, key.getString("name"));
            ((CitizensNPC) npc).load(key);

            created++;
        }
        Messaging.logTr(Messages.NUM_LOADED_NOTIFICATION, created, "?");
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

    public void storeAll(NPCRegistry registry) {
        for (NPC npc : registry)
            store(npc);
    }

    public static NPCDataStore create(File folder) {
        Storage saves = null;
        String type = Setting.STORAGE_TYPE.asString();
        if (type.equalsIgnoreCase("db") || type.equalsIgnoreCase("database")) {
            try {
                saves = new DatabaseStorage(Setting.DATABASE_DRIVER.asString(), Setting.DATABASE_URL.asString(),
                        Setting.DATABASE_USERNAME.asString(), Setting.DATABASE_PASSWORD.asString());
            } catch (SQLException e) {
                e.printStackTrace();
                Messaging.logTr(Messages.DATABASE_CONNECTION_FAILED);
            }
        } else if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(folder + File.separator + Setting.STORAGE_FILE.asString(), "Citizens NPC Storage");
        }
        if (saves == null)
            saves = new YamlStorage(new File(folder, Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
        if (!saves.load())
            return null;
        return new NPCDataStore(saves);
    }
}
