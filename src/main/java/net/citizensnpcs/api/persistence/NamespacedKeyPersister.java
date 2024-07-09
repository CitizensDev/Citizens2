package net.citizensnpcs.api.persistence;

import java.util.Locale;

import org.bukkit.NamespacedKey;

import net.citizensnpcs.api.util.DataKey;

public class NamespacedKeyPersister implements Persister<NamespacedKey> {
    @Override
    public NamespacedKey create(DataKey root) {
        String value = root.getString("");
        if (!value.contains(":")) {
            value = "minecraft:" + value.toLowerCase(Locale.ROOT);
        }
        return NamespacedKey.fromString(value);
    }

    @Override
    public void save(NamespacedKey instance, DataKey root) {
        root.setString("", instance.getNamespace() + ":" + instance.getKey());
    }
}
