package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.Creeper;

public class Powered extends Trait implements Toggleable {
    @Persist("")
    private boolean powered;

    public Powered() {
        super("powered");
    }

    @Override
    public void onSpawn() {
        if (npc.getBukkitEntity() instanceof Creeper)
            ((Creeper) npc.getBukkitEntity()).setPowered(powered);
    }

    @Override
    public boolean toggle() {
        powered = !powered;
        if (npc.getBukkitEntity() instanceof Creeper)
            ((Creeper) npc.getBukkitEntity()).setPowered(powered);
        return powered;
    }

    @Override
    public String toString() {
        return "Powered{" + powered + "}";
    }
}