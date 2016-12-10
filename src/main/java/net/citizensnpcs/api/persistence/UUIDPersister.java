package net.citizensnpcs.api.persistence;

import java.util.UUID;

import net.citizensnpcs.api.util.DataKey;

public class UUIDPersister implements Persister<UUID> {
    @Override
    public UUID create(DataKey root) {
        try {
            return UUID.fromString(root.getString(""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void save(UUID instance, DataKey root) {
        root.setString("", instance.toString());
    }
}
