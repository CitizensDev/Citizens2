package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("forcedespawntrait")
public class ForceDespawnTrait extends Trait {

    @Persist
    private boolean despawned = false;

    protected ForceDespawnTrait() {
        super("forcedespawntrait");
    }

    public boolean isDespawned() {
        return despawned;
    }

    public void setDespawned(boolean despawned) {
        this.despawned = despawned;
    }
}
