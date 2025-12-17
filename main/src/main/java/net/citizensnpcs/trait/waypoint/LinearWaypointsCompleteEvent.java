package net.citizensnpcs.trait.waypoint;

import java.util.ListIterator;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.event.CitizensEvent;

public class LinearWaypointsCompleteEvent extends CitizensEvent {
    private ListIterator<Waypoint> next;
    private final WaypointProvider provider;

    public LinearWaypointsCompleteEvent(WaypointProvider provider, ListIterator<Waypoint> next) {
        this.next = next;
        this.provider = provider;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ListIterator<Waypoint> getNextWaypoints() {
        return next;
    }

    public WaypointProvider getWaypointProvider() {
        return provider;
    }

    public void setNextWaypoints(ListIterator<Waypoint> waypoints) {
        next = waypoints;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static HandlerList handlers = new HandlerList();
}
