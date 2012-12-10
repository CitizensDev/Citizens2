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
        this.name = name.toLowerCase();
        return this;
    }

    /**
     * Constructs a factory with the given trait class. The trait class must
     * have a no-arguments constructor.
     * 
     * @param trait
     *            Class of the trait
     * @return The created {@link TraitInfo}
     * @throws IllegalArgumentException
     *             If the trait class does not have a no-arguments constructor
     */
    public static TraitInfo create(Class<? extends Trait> trait) {
        try {
            trait.getConstructor(new Class<?>[] {});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Trait class must have a no-arguments constructor");
        }
        return new TraitInfo(trait);
    }
}