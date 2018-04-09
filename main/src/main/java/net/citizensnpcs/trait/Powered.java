package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Creeper;

@TraitName("powered")
public class Powered extends Trait implements Toggleable {
    @Persist("")
    private boolean powered;

    public Powered() {
        super("powered");
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Creeper) {
            ((Creeper) npc.getEntity()).setPowered(powered);
        }
    }

    @Override
    public boolean toggle() {
        powered = !powered;
        if (npc.getEntity() instanceof Creeper) {
            ((Creeper) npc.getEntity()).setPowered(powered);
        }
        return powered;
    }

    @Override
    public String toString() {
        return "Powered{" + powered + "}";
    }
}