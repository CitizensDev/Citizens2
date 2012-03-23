package net.citizensnpcs;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

// TODO: Move to Entity.update()?
public class NPCUpdater implements Runnable {
    private final CitizensNPCManager npcManager;

    public NPCUpdater(CitizensNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        if (!npcManager.iterator().hasNext())
            return;
        for (NPC npc : npcManager) {
            if (!npc.isSpawned())
                continue;
            ((CitizensNPC) npc).update();
        }
    }
}