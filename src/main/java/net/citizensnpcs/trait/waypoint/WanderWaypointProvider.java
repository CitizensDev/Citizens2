package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.Player;

public class WanderWaypointProvider implements WaypointProvider {
    private Goal currentGoal;
    private volatile boolean paused;
    private int xrange, yrange;

    @Override
    public WaypointEditor createEditor(Player player) {
        return new WaypointEditor() {
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
        if (currentGoal == null) {
            currentGoal = WanderGoal.createWithNPCAndRange(npc, xrange, yrange);
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

    private static final int DEFAULT_XRANGE = 3;
    private static final int DEFAULT_YRANGE = 25;
}
