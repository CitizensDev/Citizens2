package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.npc.NPC;

public interface TraitFactory {

    /**
     * Adds all default traits to a given NPC.
     * 
     * @param npc
     *            The NPC to add default traits to
     */
    void addDefaultTraits(NPC npc);

    /**
     * Gets a trait with the given class.
     * 
     * @param clazz
     *            Class of the trait
     * @return Trait with the given class
     */
    <T extends Trait> T getTrait(Class<T> clazz);

    /**
     * Gets a trait with the given name.
     * 
     * @param name
     *            Name of the trait
     * @return Trait with the given name
     */
    <T extends Trait> T getTrait(String name);

    /**
     * Gets the {@link Trait} class with the given name, or null if not found.
     * 
     * @param name
     *            The trait name
     * @return The trait class
     */
    Class<? extends Trait> getTraitClass(String name);

    /**
     * Checks whether the given trait is 'internal'. An internal trait is
     * implementation-defined and is default or built-in.
     * 
     * @param trait
     *            The trait to check
     * @return Whether the trait is an internal trait
     */
    boolean isInternalTrait(Trait trait);

    /**
     * Registers a trait using the given information.
     * 
     * @param info
     *            Registration information
     */
    void registerTrait(TraitInfo info);
}