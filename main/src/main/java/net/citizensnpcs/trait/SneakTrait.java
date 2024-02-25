package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("sneak")
public class SneakTrait extends Trait {
    @Persist
    private boolean sneaking = true;

    public SneakTrait() {
        super("sneak");
    }

    private void apply() {
        if (npc.isSpawned()) {
            NMS.setSneaking(npc.getEntity(), sneaking);
        }
    }

    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public void onSpawn() {
        apply();
    }

    public void setSneaking(boolean sneak) {
        sneaking = sneak;
        apply();
    }
}