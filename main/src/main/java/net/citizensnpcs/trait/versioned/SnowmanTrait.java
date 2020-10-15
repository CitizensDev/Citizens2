package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Snowman;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("snowmantrait")
public class SnowmanTrait extends Trait {
    @Persist("derp")
    private boolean derp;

    public SnowmanTrait() {
        super("snowmantrait");
    }

    public boolean isDerp() {
        return derp;
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Snowman) {
            ((Snowman) npc.getEntity()).setDerp(derp);
        }
    }

    public void setDerp(boolean derp) {
        this.derp = derp;
    }

    public boolean toggleDerp() {
        return this.derp = !this.derp;
    }
}
