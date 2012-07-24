package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.NPC;

public interface NPCHolder {
    public NPC getNPC();

    boolean isPushable();

    void setPushable(boolean pushable);
}
