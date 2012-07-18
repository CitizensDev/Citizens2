package net.citizensnpcs.api.trait;


public interface TraitFactory {

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
     * Registers a trait using the given information.
     * 
     * @param info
     *            Registration information
     */
    public void registerTrait(TraitInfo info);
}