package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.npc.NPC;

public interface Factory<T> {
    T create(NPC npc);
}
