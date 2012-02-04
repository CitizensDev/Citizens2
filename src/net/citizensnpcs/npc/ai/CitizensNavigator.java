package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.ai.NavigatorCallback;
import net.citizensnpcs.npc.CitizensNPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CitizensNavigator implements Navigator {
    private final CitizensNPC npc;
    private PathStrategy executing;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void registerCallback(NavigatorCallback callback) {
    }

    public void update() {
        if (executing != null) {
            executing.update();
        }
    }

    @Override
    public void setDestination(Location destination) {
        executing = new MoveStrategy(npc, destination);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        executing = new TargetStrategy(npc, target, aggressive);
    }
}