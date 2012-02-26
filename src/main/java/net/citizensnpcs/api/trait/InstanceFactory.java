package net.citizensnpcs.api.trait;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents an object that can produce objects of a given type. Factories are
 * linked to a name, and produce objects on demand.
 * 
 * @param <T>
 *            Type of objects this will produce
 */
public interface InstanceFactory<T> {

    public T getInstance(String name, NPC npc);

    public T getInstance(String name);

    public void register(Class<? extends T> clazz);

    public void register(Class<? extends T> clazz, String name);

    public void registerAll(Collection<Class<? extends T>> classes);

    public void registerAll(Class<?>... classes);

    public void registerWithFactory(String name, Factory<? extends T> factory);
}