package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.npc.NPC;

public interface TraitManager {

    /**
     * Internal use only.
     */
    public <T extends Trait> T getTrait(String name, NPC npc);

    /**
     * Internal use only.
     */
    public <T extends Trait> T getTrait(Class<T> clazz, NPC npc);

    /**
     * Gets a trait with the given class.
     * 
     * @param clazz
     *            Class of the trait
     * @return Trait with the given class
     */
    public <T extends Trait> T getTrait(Class<T> clazz);

    /**
     * Gets a trait with the given name.
     * 
     * @param name
     *            Name of the trait
     * @return Trait with the given name
     */
    public <T extends Trait> T getTrait(String name);

    /**
     * Registers a trait using the given factory.
     * 
     * @param factory
     *            Factory to use to register a trait with
     */
    public void registerTrait(TraitFactory factory);
}