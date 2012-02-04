package net.citizensnpcs;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.CitizensNavigator;

public class NPCUpdater implements Runnable {
    private final CitizensNPCManager npcManager;

    public NPCUpdater(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        for (NPC npc : npcManager)
            ((CitizensNavigator) npc.getNavigator()).update();
    }
}