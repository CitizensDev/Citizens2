package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.npc.NPC;

public interface TraitManager {

    /**
     * Gets a trait with the given class.
     * 
     * @param clazz
     *            Class of the trait
     * @return Trait with the given class
     */
    public <T extends Trait> T getTrait(Class<T> clazz, NPC npc);

    /**
     * Gets a trait with the given name.
     * 
     * @param name
     *            Name of the trait
     * @return Trait with the given name
     */
    public <T extends Trait> T getTrait(String name, NPC npc);

    /**
     * Registers a trait using the given information.
     * 
     * @param info
     *            Registration information
     */
    public void registerTrait(TraitInfo info);
}