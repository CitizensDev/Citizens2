package net.citizensnpcs.api.ai.event;

import org.bukkit.event.Event;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;

public abstract class NavigationEvent extends Event {
    private final Navigator navigator;

    protected NavigationEvent(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     * @return The {@link Navigator} involved in this event
     */
    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * @return The {@link NPC} involved in this event
     */
    public NPC getNPC() {
        return navigator.getNPC();
    }
}
