package net.citizensnpcs.trait;

import org.bukkit.entity.Ocelot;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.trait.versioned.CatTrait;
import net.citizensnpcs.util.NMS;

/**
 * Persists Ocelot metadata.
 *
 * @see Ocelot
 */
@TraitName("ocelotmodifiers")
public class OcelotModifiers extends Trait {
    @Persist("sitting")
    private boolean sitting;
    @Persist("type")
    private Ocelot.Type type = Ocelot.Type.WILD_OCELOT;

    public OcelotModifiers() {
        super("ocelotmodifiers");
    }

    @Override
    public void onSpawn() {
        updateModifiers();
    }

    public void setSitting(boolean sit) {
        this.sitting = sit;
        updateModifiers();
    }

    public void setType(Ocelot.Type type) {
        this.type = type;
        updateModifiers();
    }

    private void updateModifiers() {
        if (npc.getEntity() instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) npc.getEntity();
            try {
                ocelot.setCatType(type);
            } catch (UnsupportedOperationException ex) {
                npc.getTrait(CatTrait.class).setSitting(sitting);
                npc.getTrait(CatTrait.class).setType(type);
                npc.removeTrait(OcelotModifiers.class);
                return;
            }
            NMS.setSitting(ocelot, sitting);
        }
    }
}
