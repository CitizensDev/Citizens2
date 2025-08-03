package net.citizensnpcs.trait;

import org.bukkit.entity.Enderman;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Persists {@link Enderman} metadata.
 */
@TraitName("endermantrait")
public class EndermanTrait extends Trait {
    @Persist("angry")
    private boolean angry;

    public EndermanTrait() {
        super("endermantrait");
    }

    public boolean isAngry() {
        return angry;
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Enderman) {
            Enderman enderman = (Enderman) npc.getEntity();
            NMS.setEndermanAngry(enderman, angry);
        }
    }

    public boolean toggleAngry() {
        return angry = !angry;
    }
}
