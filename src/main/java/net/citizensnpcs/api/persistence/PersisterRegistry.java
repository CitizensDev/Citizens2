package net.citizensnpcs.api.persistence;

import java.lang.ref.WeakReference;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.util.DataKey;

/**
 * A stringly-typed registry that loads and saves its types using {@link PersistenceLoader} and {@link DataKey}s.
 **/
public class PersisterRegistry<T> implements Persister<T> {
    private final Map<String, WeakReference<Class<? extends T>>> registry = Maps.newHashMap();

    PersisterRegistry() {
    }

    @Override
    public T create(DataKey root) {
        String type = root.getString("type");
        WeakReference<Class<? extends T>> clazz = registry.get(type);
        if (clazz == null)
            throw new IllegalStateException("missing registration for type " + type);
        return PersistenceLoader.load(clazz.get(), root);
    }

    public void register(String type, Class<? extends T> clazz) {
        registry.put(type, new WeakReference<Class<? extends T>>(clazz));
    }

    public Iterable<Class<? extends T>> registeredTypes() {
        return Iterables.transform(registry.values(), ref -> ref.get());
    }

    @Override
    public void save(T instance, DataKey root) {
        PersistenceLoader.save(instance, root);
        Class<?> clazz = instance.getClass();
        for (Map.Entry<String, WeakReference<Class<? extends T>>> entry : registry.entrySet()) {
            if (clazz == entry.getValue().get()) {
                root.setString("type", entry.getKey());
                return;
            }
        }
        throw new IllegalStateException("missing registration for instance " + instance);
    }
}
