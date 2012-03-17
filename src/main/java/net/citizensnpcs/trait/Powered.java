package net.citizensnpcs.trait;

import org.bukkit.entity.Creeper;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class Powered extends Trait implements Toggleable {
    private boolean powered;
    private final NPC npc;

    public Powered(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) {
        powered = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", powered);
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Creeper)
            ((Creeper) npc.getBukkitEntity()).setPowered(powered);
    }

    @Override
    public boolean toggle() {
        powered = !powered;
        ((Creeper) npc.getBukkitEntity()).setPowered(powered);
        return powered;
    }

    @Override
    public String toString() {
        return "Powered{" + powered + "}";
    }
}