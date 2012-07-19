package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

public class WaypointGoal implements Goal {
    private Location currentDestination;
    private Iterator<Waypoint> itr;
    private final Navigator navigator;
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

    @EventHandler
    public void onNavigationCancel(NavigationCancelEvent event) {
        if (!event.getNavigator().equals(navigator) || currentDestination == null)
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

    @Override
    public boolean shouldExecute(GoalSelector selector) {
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
