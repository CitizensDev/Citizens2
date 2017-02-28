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

    private boolean canStand(Block block) {
        return MinecraftBlockExaminer.canStandIn(block.getType())
                && MinecraftBlockExaminer.canStandIn(block.getRelative(BlockFace.UP).getType());
    }

    @Override
    public boolean run(NPC npc, Navigator navigator) {
        if (!npc.isSpawned())
            return false;
        Location base = navigator.getTargetAsLocation();
        if (npc.getEntity().getWorld().equals(base.getWorld())
                && npc.getEntity().getLocation().distanceSquared(base) <= RANGE) {
            return true;
        }
        Block block = base.getBlock();
        int iterations = 0;
        while (!canStand(block)) {
            if (iterations++ >= MAX_ITERATIONS) {
                block = base.getBlock();
                break;
            }
            block = block.getRelative(BlockFace.UP);
        }
        npc.teleport(block.getLocation(), TeleportCause.PLUGIN);
        return false;
    }

    public static TeleportStuckAction INSTANCE = new TeleportStuckAction();
    private static final int MAX_ITERATIONS = 10;
    private static final double RANGE = 10;
}
