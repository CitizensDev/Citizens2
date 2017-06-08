package net.citizensnpcs.nms.v1_12_R1.trait;

import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("parrottrait")
public class ParrotTrait extends Trait {
    @Persist
    private Variant variant = Variant.BLUE;

    public ParrotTrait() {
        super("parrottrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Parrot) {
            Parrot parrot = (Parrot) npc.getEntity();
            parrot.setVariant(variant);
        }
    }

    public void setVariant(Parrot.Variant variant) {
        this.variant = variant;
    }
}
