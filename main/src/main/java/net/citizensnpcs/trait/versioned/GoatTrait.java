package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Goat;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("goattrait")
public class GoatTrait extends Trait {
    @Persist
    private boolean leftHorn = true;
    @Persist
    private boolean rightHorn = true;

    public GoatTrait() {
        super("goattrait");
    }

    public boolean isLeftHorn() {
        return leftHorn;
    }

    public boolean isRightHorn() {
        return rightHorn;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Goat) {
            Goat goat = (Goat) npc.getEntity();
            goat.setRightHorn(rightHorn);
            goat.setLeftHorn(leftHorn);
        }
    }

    public void setLeftHorn(boolean horn) {
        this.leftHorn = horn;
    }

    public void setRightHorn(boolean horn) {
        this.rightHorn = horn;
    }
}
