package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

public interface NPCFlock {
    public Collection<NPC> getNearby(NPC npc);
}
