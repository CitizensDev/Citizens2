package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.PufferFish;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("pufferfishtrait")
public class PufferFishTrait extends Trait {
    @Persist
    private int puffState = 1;

    public PufferFishTrait() {
        super("pufferfishtrait");
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof PufferFish) {
            PufferFish puffer = (PufferFish) npc.getEntity();
            puffer.setPuffState(puffState);
        }
    }

    public void setPuffState(int state) {
        this.puffState = state;
    }
}
