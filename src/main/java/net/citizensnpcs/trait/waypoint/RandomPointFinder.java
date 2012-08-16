package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import org.bukkit.Location;

public class RandomPointFinder implements Iterator<Location> {

    public Location find() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Location next() {
        return find();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
