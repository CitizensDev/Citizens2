package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.TraitManager;

public interface CitizensPlugin {
    public NPCManager getNPCManager();

    public TraitManager getTraitManager();
}
