package net.citizensnpcs.api.npc.trait;

import java.util.HashMap;
import java.util.Map;

public class DefaultInstanceFactory<T> implements InstanceFactory<T> {
    private final Map<String, Factory<? extends T>> registered = new HashMap<String, Factory<? extends T>>();

    @Override
    public T getInstance(String name) {
        return registered.containsKey(name) ? registered.get(name).create() : null;
    }

    @Override
    public void register(String name, Class<? extends T> clazz) {
        registerWithFactory(name, new DefaultFactory(clazz));
    }

    @Override
    public void registerWithFactory(String name, Factory<? extends T> factory) {
        if (registered.get(name) != null)
            throw new IllegalArgumentException("A factory with the name '" + name + "' has already been registered.");
        registered.put(name, factory);
    }

    public static <T> InstanceFactory<T> create() {
        return new DefaultInstanceFactory<T>();
    }

    private class DefaultFactory implements Factory<T> {
        private final Class<? extends T> clazz;

        private DefaultFactory(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T create() {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
