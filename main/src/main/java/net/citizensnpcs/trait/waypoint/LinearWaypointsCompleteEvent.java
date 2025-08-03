package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.event.CitizensEvent;

public class LinearWaypointsCompleteEvent extends CitizensEvent {
    private Iterator<Waypoint> next;
    private final WaypointProvider provider;

    public LinearWaypointsCompleteEvent(WaypointProvider provider, Iterator<Waypoint> next) {
        this.next = next;
        this.provider = provider;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Iterator<Waypoint> getNextWaypoints() {
        return next;
    }

    public WaypointProvider getWaypointProvider() {
        return provider;
    }

    public void setNextWaypoints(Iterator<Waypoint> waypoints) {
        next = waypoints;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static HandlerList handlers = new HandlerList();
}
