package net.citizensnpcs.api.trait;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * Builds a trait.
 */
public final class TraitInfo {
    private boolean defaultTrait;
    private String name;
    private boolean trackStats;
    private Supplier<? extends Trait> supplier;
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
                trait.getConstructor(new Class<?>[] {});
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
        Preconditions.checkNotNull(name);
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
        Preconditions.checkNotNull(trait);
        return new TraitInfo(trait);
    }
}