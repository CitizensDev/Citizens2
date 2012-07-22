package net.citizensnpcs.api.trait;

public interface TraitFactory {

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
     * Registers a trait using the given information.
     * 
     * @param info
     *            Registration information
     */
    void registerTrait(TraitInfo info);

    /**
     * Gets the {@link Trait} class with the given name, or null if not found.
     * 
     * @param name
     *            The trait name
     * @return The trait class
     */
    Class<? extends Trait> getTraitClass(String name);
}