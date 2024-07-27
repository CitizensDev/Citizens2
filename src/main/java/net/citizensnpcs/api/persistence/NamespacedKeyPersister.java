package net.citizensnpcs.api.persistence;

import org.bukkit.NamespacedKey;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.SpigotUtil;

public class NamespacedKeyPersister implements Persister<NamespacedKey> {
    @Override
    public NamespacedKey create(DataKey root) {
        return SpigotUtil.getKey(root.getString(""));
    }

    @Override
    public void save(NamespacedKey instance, DataKey root) {
        root.setString("", instance.getNamespace() + ":" + instance.getKey());
    }
}
