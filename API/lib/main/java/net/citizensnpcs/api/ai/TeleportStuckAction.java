package net.citizensnpcs.api.ai;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;

public class TeleportStuckAction implements StuckAction {
    private TeleportStuckAction() {
        // singleton
    }

    @Override
    public boolean run(NPC npc, Navigator navigator) {
        if (!npc.isSpawned())
            return false;
        Location base = navigator.getTargetAsLocation();
        if (base == null || base.getWorld() != npc.getEntity().getWorld())
            return true;
        Block block = base.getBlock().getRelative(BlockFace.DOWN);
        int iterations = 0;
        while (!MinecraftBlockExaminer.canStandOn(block)) {
            if (iterations++ >= MAX_ITERATIONS) {
                block = base.getBlock().getRelative(BlockFace.DOWN);
                break;
            }
            block = block.getRelative(BlockFace.UP);
        }
        npc.teleport(block.getRelative(BlockFace.UP).getLocation(), TeleportCause.PLUGIN);
        return false;
    }

    @Override
    public String toString() {
        return "TeleportStuckAction";
    }

    public static TeleportStuckAction INSTANCE = new TeleportStuckAction();
    private static final int MAX_ITERATIONS = 10;
}
