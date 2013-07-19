package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;

import org.bukkit.entity.EntityType;

public class SimpleNPCDataStore implements NPCDataStore {
    private final Storage root;

    private SimpleNPCDataStore(Storage saves) {
        root = saves;
    }

    @Override
    public void clearData(NPC npc) {
        root.getKey("npc").removeKey(Integer.toString(npc.getId()));
    }

    @Override
    public void loadInto(NPCRegistry registry) {
        for (DataKey key : root.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name")) {
                Messaging.logTr(LOAD_NAME_NOT_FOUND, id);
                continue;
            }
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = matchEntityType(unparsedEntityType);
            if (type == null) {
                Messaging.logTr(LOAD_UNKNOWN_NPC_TYPE, unparsedEntityType);
                continue;
            }
            NPC npc = registry.createNPC(type, id, key.getString("name"));
            npc.load(key);
        }
    }

    @Override
    public void saveToDisk() {
        new Thread() {
            @Override
            public void run() {
                root.save();
            }
        }.start();
    }

    @Override
    public void saveToDiskImmediate() {
        root.save();
    }

    @Override
    public void store(NPC npc) {
        npc.save(root.getKey("npc." + npc.getId()));
    }

    @Override
    public void storeAll(NPCRegistry registry) {
        for (NPC npc : registry)
            store(npc);
    }

    private static final String LOAD_NAME_NOT_FOUND = "citizens.notifications.npc-name-not-found";

    private static final String LOAD_UNKNOWN_NPC_TYPE = "citizens.notifications.unknown-npc-type";

    public static NPCDataStore create(Storage storage) {
        return new SimpleNPCDataStore(storage);
    }

    private static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        return matchEnum(EntityType.values(), toMatch);
    }

    private static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        T type = null;
        for (T check : values) {
            String name = check.name();
            if (name.matches(toMatch) || name.equalsIgnoreCase(toMatch)
                    || name.replace("_", "").equalsIgnoreCase(toMatch)
                    || name.replace('_', '-').equalsIgnoreCase(toMatch)
                    || name.replace('_', ' ').equalsIgnoreCase(toMatch) || name.startsWith(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
    }
}
