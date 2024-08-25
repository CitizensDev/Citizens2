package net.citizensnpcs.trait;

import org.bukkit.entity.Bat;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("battrait")
public class BatTrait extends Trait {
    @Persist
    private boolean awake;

    public BatTrait() {
        super("battrait");
    }

    public boolean isAwake() {
        return awake;
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Bat))
            return;
        ((Bat) npc.getEntity()).setAwake(awake);
    }

    public void setAwake(boolean awake) {
        this.awake = awake;
    }
}
