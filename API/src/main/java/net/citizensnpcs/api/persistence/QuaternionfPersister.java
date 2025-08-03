package net.citizensnpcs.api.persistence;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import net.citizensnpcs.api.util.DataKey;

public class QuaternionfPersister implements Persister<Quaternionfc> {
    @Override
    public Quaternionfc create(DataKey root) {
        return new Quaternionf(root.getDouble("x"), root.getDouble("y"), root.getDouble("z"), root.getDouble("w"));
    }

    @Override
    public void save(Quaternionfc instance, DataKey root) {
        root.setDouble("x", instance.x());
        root.setDouble("y", instance.y());
        root.setDouble("z", instance.z());
        root.setDouble("w", instance.w());
    }
}
