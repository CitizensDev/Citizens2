package net.citizensnpcs.api.npc.trait;

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

    public void register(Class<? extends T> clazz);

    public void registerWithFactory(String name, Factory<? extends T> factory);
}