package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.ai.AI;
import net.citizensnpcs.api.npc.ai.Goal;
import net.citizensnpcs.api.npc.ai.NavigationCallback;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CitizensAI implements AI {
    private PathStrategy executing;
    private Runnable ai;
    private final CitizensNPC npc;

    public CitizensAI(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void addGoal(int priority, Goal goal) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerNavigationCallback(NavigationCallback callback) {
    }

    @Override
    public void setAI(Runnable ai) {
        this.ai = ai;
    }

    @Override
    public void setDestination(Location destination) {
        executing = new MoveStrategy(npc, destination);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        executing = new TargetStrategy(npc, target, aggressive);
    }

    public void update() {
        if (executing != null && executing.update()) {
            executing = null;
        }

        if (ai != null) {
            try {
                ai.run();
            } catch (Throwable ex) {
                Messaging.log("Unexpected error while running ai " + ai);
                ex.printStackTrace();
            }
        }
    }
}