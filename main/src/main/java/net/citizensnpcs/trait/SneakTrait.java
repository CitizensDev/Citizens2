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

    @Override
    public void run() {
        if (npc.data().has(NPC.Metadata.SNEAKING)) {
            setSneaking(npc.data().get(NPC.Metadata.SNEAKING));
            npc.data().remove(NPC.Metadata.SNEAKING);
        }
    }

    public void setSneaking(boolean sneak) {
        sneaking = sneak;
        apply();
    }
}