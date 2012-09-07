package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TeleportStuckAction implements StuckAction {
    private TeleportStuckAction() {
        // singleton
    }

    @Override
    public boolean run(NPC npc, Navigator navigator) {
        if (!npc.isSpawned())
            return false;
        Location base = navigator.getTargetAsLocation();
        if (npc.getBukkitEntity().getLocation().distanceSquared(base) <= RANGE)
            return true;
        Block block = base.getBlock();
        int iterations = 0;
        while (!block.isEmpty()) {
            block = block.getRelative(BlockFace.UP);
            if (++iterations >= MAX_ITERATIONS && !block.isEmpty())
                block = base.getBlock();
        }
        npc.getBukkitEntity().teleport(block.getLocation());
        return false;
    }

    public static TeleportStuckAction INSTANCE = new TeleportStuckAction();

    private static int MAX_ITERATIONS = 10;
    private static final double RANGE = 10;
}
