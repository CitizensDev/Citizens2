package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.NavigationCallback;

import org.bukkit.Location;

public class GenericWaypointCallback extends NavigationCallback {
    private Location dest;
    private boolean executing;
    private Iterator<Waypoint> itr;
    private final Iterable<Waypoint> provider;

    public GenericWaypointCallback(Iterable<Waypoint> provider) {
        this.provider = provider;
    }

    private void ensureItr() {
        if (itr == null || !itr.hasNext())
            itr = provider.iterator();
    }

    @Override
    public void onAttach(AI ai) {
        executing = !ai.hasDestination();
        if (!executing)
            return;
        if (dest == null) {
            ensureItr();
            if (itr.hasNext()) {
                dest = itr.next().getLocation();
            }
        }
        if (dest != null) {
            ai.setDestination(dest);
        }
    }

    @Override
    public boolean onCancel(AI ai, PathCancelReason reason) {
        if (executing) {
            executing = false;
        } else {
            executing = true;
            ensureItr();
            if (dest != null)
                ai.setDestination(dest);
            else if (itr.hasNext()) {
                ai.setDestination(itr.next().getLocation());
            }
        }
        return false;
    }

    @Override
    public boolean onCompletion(AI ai) {
        if (executing) { // if we're executing, we need to get the next waypoint
            if (!itr.hasNext()) {
                dest = null;
            } else {
                dest = itr.next().getLocation();
            }
        } else {
            executing = true;
            // we're free to return to our waypoints!
            // if we had a destination, we will return to it.
        }
        if (dest != null) {
            ai.setDestination(dest);
        }
        return false;
    }

    public void onProviderChanged() {
        itr = provider.iterator();
        dest = null;
    }
}
