package net.citizensnpcs;

import java.util.Map;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.trait.ShopTrait.NPCShop;

public class StoredShops {
    @Persist(value = "global", reify = true)
    public Map<String, NPCShop> globalShops = Maps.newHashMap();
    @Persist(value = "npc", reify = true)
    public Map<String, NPCShop> npcShops = Maps.newHashMap();
    private final Storage storage;

    public StoredShops(YamlStorage storage) {
        this.storage = storage;
    }

    public void deleteShop(NPCShop shop) {
        if (Messaging.isDebugging()) {
            Messaging.debug("Deleting shop", shop.getName());
        }
        if (npcShops.values().contains(shop)) {
            npcShops.values().remove(shop);
        } else {
            globalShops.values().remove(shop);
        }
    }

    public NPCShop getGlobalShop(String name) {
        return globalShops.computeIfAbsent(name, NPCShop::new);
    }

    public NPCShop getShop(String name) {
        if (npcShops.containsKey(name))
            return npcShops.get(name);
        return getGlobalShop(name);
    }

    public void load() {
        Messaging.debug("Loading shops...", globalShops.size(), npcShops.size());
        PersistenceLoader.load(this, storage.getKey(""));
    }

    public boolean loadFromDisk() {
        Messaging.debug("Loading shops from disk...");
        return storage.load();
    }

    public void saveToDisk() {
        Messaging.debug("Saving shops to disk...");
        storage.save();
    }

    public void storeShops() {
        Messaging.debug("Saving shops...", globalShops.size(), npcShops.size());
        PersistenceLoader.save(this, storage.getKey(""));
    }
}