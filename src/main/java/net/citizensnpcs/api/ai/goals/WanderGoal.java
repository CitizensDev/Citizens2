package net.citizensnpcs.api.ai.goals;

import java.util.Random;

import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;

public class WanderGoal extends BehaviorGoalAdapter {
    private final Random random = new Random();
    private boolean forceFinish;
    private final NPC npc;

    private WanderGoal(NPC npc) {
        this.npc = npc;
    }

    public static WanderGoal createWithNPC(NPC npc) {
        return new WanderGoal(npc);
    }

    private Location findRandomPosition() {
        Location base = npc.getBukkitEntity().getLocation();
        Location found = null;
        int range = 10;
        int yrange = 2;
        for (int i = 0; i < 10; i++) {
            int x = base.getBlockX() + random.nextInt(2 * range) - range;
            int y = base.getBlockY() + random.nextInt(2 * yrange) - yrange;
            int z = base.getBlockZ() + random.nextInt(2 * range) - range;
            Block block = base.getWorld().getBlockAt(x, y, z);
            if (block.isEmpty() && block.getRelative(BlockFace.DOWN).isEmpty()) {
                found = block.getLocation();
                break;
            }
        }
        return found;
    }

    @EventHandler
    public void onFinish(NavigationCompleteEvent event) {
        forceFinish = true;
    }

    @Override
    public void reset() {
        forceFinish = false;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating())
            return false;
        Location dest = findRandomPosition();
        if (dest == null)
            return false;
        npc.getNavigator().setTarget(dest);
        return true;
    }

    @Override
    public BehaviorStatus run() {
        if (!npc.getNavigator().isNavigating() || forceFinish)
            return BehaviorStatus.SUCCESS;
        return BehaviorStatus.RUNNING;
    }
}