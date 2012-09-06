package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TeleportStuckAction implements StuckAction {
    private TeleportStuckAction() {
        // singleton
    }

    @Override
    public void run(NPC npc, Navigator navigator) {
        if (!npc.isSpawned())
            return;
        Block block = navigator.getTargetAsLocation().getBlock();
        int iterations = 0;
        while (!block.isEmpty()) {
            block = block.getRelative(BlockFace.UP);
            if (++iterations >= MAX_ITERATIONS && !block.isEmpty())
                block = navigator.getTargetAsLocation().getBlock();
        }
        npc.getBukkitEntity().teleport(block.getLocation());
    }

    private static int MAX_ITERATIONS = 10;
    public static TeleportStuckAction INSTANCE = new TeleportStuckAction();
}
