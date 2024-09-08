package net.citizensnpcs.trait.shop;

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
        if (npcShops.containsKey(shop.getName())) {
            npcShops.remove(shop.getName());
        } else {
            globalShops.remove(shop.getName());
        }
    }

    public NPCShop getGlobalShop(String name) {
        return globalShops.computeIfAbsent(name, NPCShop::new);
    }

    public NPCShop getShop(String name) {
        NPCShop shop = npcShops.get(name);
        return shop == null ? getGlobalShop(name) : shop;
    }

    public void load() {
        PersistenceLoader.load(this, storage.getKey(""));
    }

    public boolean loadFromDisk() {
        return storage.load();
    }

    public void saveToDisk() {
        storage.save();
    }

    public void storeShops() {
        PersistenceLoader.save(this, storage.getKey(""));
    }
}