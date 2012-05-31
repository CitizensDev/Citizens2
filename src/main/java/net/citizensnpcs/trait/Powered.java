package net.citizensnpcs.trait;

import net.citizensnpcs.api.abstraction.entity.Creeper;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

public class Powered extends Attachment implements Toggleable {
    private final NPC npc;
    private boolean powered;

    public Powered(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        powered = key.getBoolean("");
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Creeper)
            ((Creeper) npc.getEntity()).setPowered(powered);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", powered);
    }

    @Override
    public boolean toggle() {
        powered = !powered;
        if (npc.getEntity() instanceof Creeper)
            ((Creeper) npc.getEntity()).setPowered(powered);
        return powered;
    }

    @Override
    public String toString() {
        return "Powered{" + powered + "}";
    }
}