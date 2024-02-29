package net.citizensnpcs.trait;

import org.bukkit.entity.Creeper;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists {@link Creeper} powered status.
 *
 * @see Creeper#setPowered(boolean)
 */
@TraitName("powered")
public class Powered extends Trait {
    @Persist("")
    private boolean powered;

    public Powered() {
        super("powered");
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Creeper) {
            ((Creeper) npc.getEntity()).setPowered(powered);
        }
    }

    public void setPowered(boolean value) {
        powered = value;
        onSpawn();
    }
}