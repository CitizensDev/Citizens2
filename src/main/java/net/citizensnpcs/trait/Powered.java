package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Creeper;

public class Powered extends Trait implements Toggleable {
    private final NPC npc;
    private boolean powered;

    public Powered(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (npc.isSpawned() && !(npc.getBukkitEntity() instanceof Creeper))
            throw new NPCLoadException("NPC must be a creeper");
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
        ((Creeper) npc.getBukkitEntity()).setPowered(powered);
        return powered;
    }

    @Override
    public String toString() {
        return "Powered{" + powered + "}";
    }
}