package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Fox;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("foxtrait")
public class FoxTrait extends Trait {
    @Persist
    private boolean crouching = false;
    @Persist
    private boolean sitting = false;
    @Persist
    private boolean sleeping = false;
    @Persist
    private Fox.Type type = Fox.Type.RED;

    public FoxTrait() {
        super("foxtrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Fox) {
            Fox fox = (Fox) npc.getEntity();
            fox.setSitting(sitting);
            fox.setCrouching(crouching);
            fox.setSleeping(sleeping);
            fox.setFoxType(type);
        }
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    public void setType(Fox.Type type) {
        this.type = type;
    }
}
