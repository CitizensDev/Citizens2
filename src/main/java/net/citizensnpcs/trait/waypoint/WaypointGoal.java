package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.npc.ai.NavigationCompleteEvent;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

public class WaypointGoal implements Goal {
    private Location currentDestination;
    private Iterator<Waypoint> itr;
    private final Navigator navigator;
    private boolean paused;
    private final Iterable<Waypoint> provider;
    private GoalSelector selector;

    public WaypointGoal(Iterable<Waypoint> provider, Navigator navigator) {
        this.provider = provider;
        this.navigator = navigator;
    }

    private void ensureItr() {
        if (itr == null || !itr.hasNext()) {
            itr = provider.iterator();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    @EventHandler
    public void onNavigationCancel(NavigationCancelEvent event) {
        if (currentDestination == null || !event.getNavigator().equals(navigator))
            return;
        if (currentDestination.equals(event.getNavigator().getTargetAsLocation()))
            selector.finish();
    }

    @EventHandler
    public void onNavigationComplete(NavigationCompleteEvent event) {
        if (currentDestination == null || !event.getNavigator().equals(navigator))
            return;
        if (currentDestination.equals(event.getNavigator().getTargetAsLocation()))
            selector.finish();
    }

    public void onProviderChanged() {
        itr = provider.iterator();
        if (currentDestination != null)
            selector.finish();
    }

    @Override
    public void reset() {
        currentDestination = null;
        selector = null;
    }

    @Override
    public void run() {
    }

    public void setPaused(boolean paused) {
        if (paused && currentDestination != null)
            selector.finish();
        this.paused = paused;
    }

    @Override
    public boolean shouldExecute(GoalSelector selector) {
        if (paused || currentDestination != null)
            return false;
        ensureItr();
        boolean shouldExecute = itr.hasNext();
        if (shouldExecute) {
            this.selector = selector;
            currentDestination = itr.next().getLocation();
            navigator.setTarget(currentDestination);
        }
        return shouldExecute;
    }
}
