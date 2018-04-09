package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;
import org.bukkit.entity.Wither;

@TraitName("withertrait")
public class WitherTrait extends Trait {
    @Persist("charged")
    private boolean charged = false;

    public WitherTrait() {
        super("withertrait");
    }

    public boolean isCharged() {
        return charged;
    }

    @Override
    public void onSpawn() {
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Wither) {
            Wither wither = (Wither) npc.getEntity();
            NMS.setWitherCharged(wither, charged);
        }
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }
}
