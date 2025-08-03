package net.citizensnpcs.api.persistence;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.util.DataKey;

public class VectorPersister implements Persister<Vector> {
    @Override
    public Vector create(DataKey root) {
        return new Vector(root.getDouble("x"), root.getDouble("y"), root.getDouble("z"));
    }

    @Override
    public void save(Vector instance, DataKey root) {
        root.setDouble("x", instance.getX());
        root.setDouble("y", instance.getY());
        root.setDouble("z", instance.getZ());
    }
}
