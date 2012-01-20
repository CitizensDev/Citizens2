package net.citizensnpcs.api.npc.trait;

import java.util.Collection;

public interface TraitManager {

    /**
     * Registers a trait to Citizens
     * 
     * @param trait
     *            Trait to register
     */
    public void registerTrait(Class<? extends Trait> trait);

    /**
     * Gets a trait from the given name
     * 
     * @param trait
     *            Trait to register
     */
    public Trait getTrait(String name);

    /**
     * Gets all registered traits
     * 
     * @return All registered traits
     */
    public Collection<Trait> getRegisteredTraits();
}