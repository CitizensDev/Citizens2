package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.PolarBear;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("polarbeartrait")
public class PolarBearTrait extends Trait {
    @Persist
    private boolean rearing;

    public PolarBearTrait() {
        super("polarbeartrait");
    }

    public boolean isRearing() {
        return rearing;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof PolarBear) {
            NMS.setPolarBearRearing(npc.getEntity(), rearing);
        }
    }

    public void setRearing(boolean rearing) {
        this.rearing = rearing;
    }
}
