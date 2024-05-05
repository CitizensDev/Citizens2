package net.citizensnpcs.api.trait;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Builds a trait.
 */
public final class TraitInfo {
    private boolean defaultTrait;
    private String name;
    private Supplier<? extends Trait> supplier;
    private boolean trackStats;
    private final Class<? extends Trait> trait;
    private boolean triedAnnotation;

    private TraitInfo(Class<? extends Trait> trait) {
        this.trait = trait;
    }

    public TraitInfo asDefaultTrait() {
        this.defaultTrait = true;
        return this;
    }

    public void checkValid() {
        if (supplier == null) {
            try {
                trait.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Trait class must have a no-arguments constructor");
            }
        }
    }

    public Class<? extends Trait> getTraitClass() {
        return trait;
    }

    public String getTraitName() {
        if (name == null && !triedAnnotation) {
            TraitName anno = trait.getAnnotation(TraitName.class);
            if (anno != null) {
                name = anno.value().toLowerCase();
            }
            triedAnnotation = true;
        }
        return name;
    }

    public boolean isDefaultTrait() {
        return defaultTrait;
    }

    public TraitInfo optInToStats() {
        this.trackStats = true;
        return this;
    }

    public boolean trackStats() {
        return trackStats;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T tryCreateInstance() {
        if (supplier != null)
            return (T) supplier.get();
        try {
            return (T) trait.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public TraitInfo withName(String name) {
        Objects.requireNonNull(name);
        this.name = name.toLowerCase();
        return this;
    }

    public TraitInfo withSupplier(Supplier<? extends Trait> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Constructs a factory with the given trait class. The trait class must have a no-arguments constructor.
     *
     * @param trait
     *            Class of the trait
     * @return The created {@link TraitInfo}
     * @throws IllegalArgumentException
     *             If the trait class does not have a no-arguments constructor
     */
    public static TraitInfo create(Class<? extends Trait> trait) {
        Objects.requireNonNull(trait);
        return new TraitInfo(trait);
    }
}