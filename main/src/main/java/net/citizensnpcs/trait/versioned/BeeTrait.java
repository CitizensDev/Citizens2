package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Bee;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("beetrait")
public class BeeTrait extends Trait {
    @Persist
    private int anger;
    @Persist
    private boolean nectar = false;
    @Persist
    private boolean stung = false;

    public BeeTrait() {
        super("beetrait");
    }

    public boolean hasNectar() {
        return nectar;
    }

    public boolean hasStung() {
        return stung;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Bee) {
            Bee bee = (Bee) npc.getEntity();
            bee.setHasStung(stung);
            bee.setAnger(anger);
            bee.setHasNectar(nectar);
        }
    }

    public void setAnger(int anger) {
        this.anger = anger;
    }

    public void setNectar(boolean nectar) {
        this.nectar = nectar;
    }

    public void setStung(boolean stung) {
        this.stung = stung;
    }
}
