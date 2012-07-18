package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;

import org.bukkit.event.Event;

public abstract class NavigationEvent extends Event {
    private final Navigator navigator;

    protected NavigationEvent(Navigator navigator) {
        this.navigator = navigator;
    }

    public Navigator getNavigator() {
        return navigator;
    }
}
