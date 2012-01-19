package net.citizensnpcs.api.event;

import org.bukkit.event.Event;

/**
 * Represents an event thrown by Citizens
 */
public class CitizensEvent extends Event {
    private static final long serialVersionUID = 6152438139474982906L;

    protected CitizensEvent(String name) {
        super(name);
    }
}