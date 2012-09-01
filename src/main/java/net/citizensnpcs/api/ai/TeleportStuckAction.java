package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;

public class TeleportStuckAction implements StuckAction {
    private TeleportStuckAction() {
        // singleton
    }

    @Override
    public void run(NPC npc, Navigator navigator) {
        if (!npc.isSpawned())
            return;
        npc.getBukkitEntity().teleport(navigator.getTargetAsLocation());
    }

    public static TeleportStuckAction INSTANCE = new TeleportStuckAction();
}
