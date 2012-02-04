package net.citizensnpcs;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

public class NPCUpdater implements Runnable {
    private final CitizensNPCManager npcManager;

    public NPCUpdater(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        for (NPC npc : npcManager)
            ((CitizensNPC) npc).update();
    }
}