package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.npc.ai.Navigator;
import net.citizensnpcs.api.npc.ai.NavigatorCallback;
import net.citizensnpcs.npc.CitizensNPC;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CitizensNavigator implements Navigator {
    private final CitizensNPC npc;

    public CitizensNavigator(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void setDestination(Location destination) {
    }

    @Override
    public void registerCallback(NavigatorCallback callback) {
    }

    @Override
    public void setTarget(Entity target, boolean aggressive) {
    }
}