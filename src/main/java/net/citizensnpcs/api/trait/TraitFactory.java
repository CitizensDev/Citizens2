package net.citizensnpcs.api.trait;

/**
 * Builds a trait.
 */
public class TraitFactory {
    private Class<? extends Trait> trait;
    private String name;

    /**
     * Constructs a factory with the given trait class.
     * 
     * @param character
     *            Class of the trait
     */
    public TraitFactory(Class<? extends Trait> trait) {
        this.trait = trait;
    }

    public final String getName() {
        return name;
    }

    public final Class<? extends Trait> getTraitClass() {
        return trait;
    }

    /**
     * Assigns a name to the trait. This is used as a key to save trait data.
     * 
     * @param name
     *            Name to assign to the trait
     */
    public final TraitFactory withName(String name) {
        this.name = name;
        return this;
    }
}