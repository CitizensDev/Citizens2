package net.citizensnpcs.api.trait;

/**
 * Builds a trait.
 */
public final class TraitInfo {
    private String name;
    private final Class<? extends Trait> trait;

    private TraitInfo(Class<? extends Trait> trait) {
        this.trait = trait;
    }

    public Class<? extends Trait> getTraitClass() {
        return trait;
    }

    public String getTraitName() {
        return name;
    }

    public TraitInfo withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Constructs a factory with the given trait class.
     * 
     * @param character
     *            Class of the trait
     */
    public static TraitInfo create(Class<? extends Trait> trait) {
        return new TraitInfo(trait);
    }
}