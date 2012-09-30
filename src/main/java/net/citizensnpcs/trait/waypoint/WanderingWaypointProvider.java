package net.citizensnpcs.trait.waypoint;

import java.util.Random;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class WanderingWaypointProvider implements WaypointProvider {
    private WanderGoal currentGoal;
    private NPC npc;
    private volatile boolean paused;
    private int xrange, yrange;

    @Override
    public Editor createEditor(Player player) {
        return new Editor() {
            @Override
            public void begin() {
                // TODO Auto-generated method stub

            }

            @Override
            public void end() {
                // TODO Auto-generated method stub

            }
        };
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void load(DataKey key) {
        xrange = key.getInt("xrange", DEFAULT_XRANGE);
        yrange = key.getInt("yrange", DEFAULT_YRANGE);
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = new WanderGoal();
            CitizensAPI.registerEvents(currentGoal);
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);
    }

    @Override
    public void save(DataKey key) {
        key.setInt("xrange", xrange);
        key.setInt("yrange", yrange);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private class WanderGoal implements Goal {
        private final Random random = new Random();
        private GoalSelector selector;

        private Location findRandomPosition() {
            Location base = npc.getBukkitEntity().getLocation();
            Location found = null;
            int range = 50;
            int yrange = 3;
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
            if (selector != null)
                selector.finish();
        }

        @Override
        public void reset() {
            selector = null;
        }

        @Override
        public void run(GoalSelector selector) {
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            if (!npc.isSpawned() || npc.getNavigator().isNavigating())
                return false;
            Location dest = findRandomPosition();
            if (dest == null)
                return false;
            npc.getNavigator().setTarget(dest);
            this.selector = selector;
            return true;
        }
    }

    private static final int DEFAULT_XRANGE = 3;

    private static final int DEFAULT_YRANGE = 25;
}
