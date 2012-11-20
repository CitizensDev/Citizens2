package net.citizensnpcs.api.astar;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class AStarNode implements Comparable<AStarNode> {
    float f, g, h;
    AStarNode parent;

    public abstract Plan buildPlan();

    @Override
    public int compareTo(AStarNode other) {
        return Float.compare(f, other.f);
    }

    public abstract Iterable<AStarNode> getNeighbours();

    protected float getPathCost() {
        return f;
    }

    protected AStarNode getParent() {
        return parent;
    }

    List<AStarNode> parents;

    @SuppressWarnings("unchecked")
    protected <T extends AStarNode> Iterable<T> getParents() {
        if (parents != null)
            return (Iterable<T>) parents;
        parents = Lists.newArrayList();
        AStarNode start = this;
        while (start != null) {
            parents.add(start);
            start = start.parent;
        }
        Collections.reverse(parents);
        return (Iterable<T>) parents;
    }
}
