package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import org.bukkit.Location;

public class PassiveWaypointCycler {
    private Location dest;
    private boolean executing;
    private Iterator<Waypoint> itr;

    private final Iterable<Waypoint> provider;

    public PassiveWaypointCycler(Iterable<Waypoint> provider) {
        this.provider = provider;
    }

    private void ensureItr() {
        if (itr == null || !itr.hasNext()) {
            itr = provider.iterator();
        }
    }

    /*@Override
    public boolean onCancel(AI ai, CancelReason reason) {
        if (hackfix) {
            hackfix = false;
            return false;
        }
        hackfix = false;
        if (executing && reason == CancelReason.REPLACE) {
            executing = false;
            return false;
        }
        executing = true;
        ensureItr();
        if (dest == null && itr.hasNext())
            dest = itr.next().getLocation();
        if (dest != null) {
            hackfix = true;
            ai.setDestination(dest);
            hackfix = false;
        }
        return false;
    }

    @Override
    public boolean onCompletion(AI ai) {
        if (executing) { // if we're executing, we need to get the next waypoint
            ensureItr();
            dest = itr.hasNext() ? itr.next().getLocation() : null;
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
        if (ai == null)
            return;
        dest = itr.hasNext() ? itr.next().getLocation() : null;
        if (dest != null) {
            ai.setDestination(dest);
        }
    }*/

    public void onProviderChanged() {
    }
}
