package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Creeper;

public class Powered extends Trait implements Toggleable {
    private boolean powered;

    public Powered() {
        super("powered");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        powered = key.getBoolean("");
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Creeper)
            ((Creeper) npc.getBukkitEntity()).setPowered(powered);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", powered);
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