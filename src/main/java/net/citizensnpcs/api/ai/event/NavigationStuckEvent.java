package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;

import org.bukkit.event.HandlerList;

public class NavigationStuckEvent extends NavigationEvent {
    private StuckAction action;

    public NavigationStuckEvent(Navigator navigator, StuckAction action) {
        super(navigator);
        this.action = action;
    }

    public StuckAction getAction() {
        return action;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setAction(StuckAction action) {
        this.action = action;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
