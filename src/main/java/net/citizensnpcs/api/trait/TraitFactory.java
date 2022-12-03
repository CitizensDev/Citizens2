package net.citizensnpcs.api.trait;

import java.util.Collection;

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
     * Removes a trait. This prevents a trait from being added to an NPC but does not remove existing traits from the
     * NPCs.
     *
     * @param info
     *            The TraitInfo to deregister
     */
    void deregisterTrait(TraitInfo info);

    /**
     * Returns all currently registered traits, including <em>internal</em> traits
     *
     * @return
     */
    Collection<TraitInfo> getRegisteredTraits();

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
     * Registers a trait using the given information.
     *
     * @param info
     *            Registration information
     */
    void registerTrait(TraitInfo info);
}