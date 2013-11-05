package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Ocelot;

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
            ocelot.setCatType(type);
            ocelot.setSitting(sitting);
        }
    }
}
