package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.Factory;

public interface TraitManager {

    /**
     * Registers a trait with the given name. This will create a factory for
     * instantiating the {@link Trait}s.
     * 
     * Note that this may be slower than
     * {@link TraitManager#registerTraitWithFactory(String, Factory)}.
     * 
     * @param name
     *            The name of the trait
     * @param clazz
     *            The trait's class
     */
    public void registerTrait(String name, Class<? extends Trait> clazz);

    /**
     * Registers an {@link Trait} with the given name. New instances of the
     * trait will be constructed with the given {@link Factory}.
     * 
     * @param name
     *            The name of the trait
     * @param factory
     *            The factory to instantiate new instances
     */
    public void registerTraitWithFactory(String name, Factory<? extends Trait> factory);

    /**
     * Constructs a trait from the given name
     * 
     * @param name
     *            Name of the trait
     */
    public Trait getTrait(String name);
}