package net.citizensnpcs.trait.versioned;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("pufferfishtrait")
public class PufferFishTrait extends Trait {
    @Persist
    private int puffState = 0;

    public PufferFishTrait() {
        super("pufferfishtrait");
    }

    public int getPuffState() {
        return puffState;
    }

    public void setPuffState(int state) {
        this.puffState = state;
    }
}
