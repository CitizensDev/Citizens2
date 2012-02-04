package net.citizensnpcs;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.entity.CitizensHumanNPC;

public class NPCUpdater implements Runnable {
    private final CitizensNPCManager npcManager;

    public NPCUpdater(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        for (NPC npc : npcManager)
            // For now only do this for human NPCs
            if (npc instanceof CitizensHumanNPC) {
                ((CitizensHumanNPC) npc).tick();
            }
    }
}