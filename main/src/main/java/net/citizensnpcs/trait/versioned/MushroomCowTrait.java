package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("mushroomcowtrait")
public class MushroomCowTrait extends Trait {
    @Persist("variant")
    private Variant variant;

    public MushroomCowTrait() {
        super("mushroomcowtrait");
    }

    @Override
    public void onSpawn() {
        setVariant(variant);
    }

    @Override
    public void run() {
        if (variant != null && npc.getEntity() instanceof MushroomCow) {
            ((MushroomCow) npc.getEntity()).setVariant(variant);
        }
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
