package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public class WanderingWaypointProvider implements WaypointProvider {
    private NPC npc;
    private boolean paused;

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
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        /*
        if (currentGoal == null) {
            currentGoal = new WaypointGoal(this, npc.getNavigator());
            CitizensAPI.registerEvents(currentGoal);
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);TODO*/
    }

    @Override
    public void save(DataKey key) {
    }

    @Override
    public void setPaused(boolean paused) {
        // TODO
    }
}
