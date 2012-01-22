package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.Factory;

public interface TraitManager {

    public void registerTraitFactory(String name, Factory<? extends Trait> factory);

    /**
     * Gets a trait from the given name
     * 
     * @param trait
     *            Trait to register
     */
    public Trait getTrait(String name);
}