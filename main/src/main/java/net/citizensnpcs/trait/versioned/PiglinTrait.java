package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Piglin;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("piglintrait")
public class PiglinTrait extends Trait {
    @Persist
    private boolean dancing;

    public PiglinTrait() {
        super("piglintrait");
    }

    public boolean isDancing() {
        return dancing;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Piglin) {
            NMS.setPiglinDancing(npc.getEntity(), dancing);
        }
    }

    public void setDancing(boolean dancing) {
        this.dancing = dancing;
    }

}
