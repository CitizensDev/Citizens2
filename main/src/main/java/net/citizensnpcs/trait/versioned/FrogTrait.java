package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("frogtrait")
public class FrogTrait extends Trait {
    @Persist
    private Variant variant;

    public FrogTrait() {
        super("frogtrait");
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Frog && variant != null) {
            Frog frog = (Frog) npc.getEntity();
            frog.setVariant(variant);
        }
    }

    public void setVariant(Frog.Variant variant) {
        this.variant = variant;
    }
}
