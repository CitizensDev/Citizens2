package net.citizensnpcs.trait;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("sneak")
public class SneakTrait extends Trait {
    @Persist
    private boolean sneaking = false;

    public SneakTrait() {
        super("sneak");
    }

    private void apply() {
        NMS.setSneaking(npc.getEntity(), sneaking);
    }

    @Override
    public void onSpawn() {
        apply();
    }

    @Override
    public void run() {
        if (npc.data().has(NPC.SNEAKING_METADATA)) {
            setSneaking(npc.data().get(NPC.SNEAKING_METADATA));
            npc.data().remove(NPC.SNEAKING_METADATA);
        }
    }

    public void setSneaking(boolean sneak) {
        this.sneaking = sneak;
        if (npc.isSpawned()) {
            apply();
        }
    }
}