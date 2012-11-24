package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import net.citizensnpcs.api.astar.Plan;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class LocationPlan implements Plan {
    private int index = 0;
    private final Vector[] path;

    public LocationPlan(Iterable<LocationNode> unfiltered) {
        this.path = cull(unfiltered);
    }

    private Vector[] cull(Iterable<LocationNode> unfiltered) {
        // possibly expose cullability in an API
        List<Vector> path = Lists.newArrayList();
        Vector last = null;
        for (LocationNode node : unfiltered) {
            Vector vector = node.location;
            if (last != null && vector.getBlockY() == last.getBlockY()) {
                if (node.blockSource.getMaterialAt(vector) == Material.AIR
                        && node.blockSource.getMaterialAt(last) == Material.AIR)
                    continue;
            }
            last = vector;
            path.add(vector);
        }
        return path.toArray(new Vector[path.size()]);
    }

    public Vector getCurrentVector() {
        return path[index];
    }

    @Override
    public boolean isComplete() {
        return index >= path.length;
    }

    @Override
    public void update() {
        ++index;
    }
}