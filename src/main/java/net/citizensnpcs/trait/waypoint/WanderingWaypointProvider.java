package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WanderingWaypointProvider implements WaypointProvider, Iterable<Location> {
    private WaypointGoal currentGoal;
    private final Iterator<Location> iterator = new RandomPointFinder();
    private NPC npc;

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
        return currentGoal.isPaused();
    }

    @Override
    public void load(DataKey key) {
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = new WaypointGoal(this, npc.getNavigator());
            CitizensAPI.registerEvents(currentGoal);
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);
    }

    @Override
    public void save(DataKey key) {
    }

    @Override
    public void setPaused(boolean paused) {
        currentGoal.setPaused(paused);
    }

    @Override
    public Iterator<Location> iterator() {
        return iterator;
    }
}
