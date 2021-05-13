package net.citizensnpcs.api.astar;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class AStarNode implements Comparable<AStarNode> {
    float g, h;
    AStarNode parent;
    List<AStarNode> parents;

    protected AStarNode(AStarNode parent) {
        this.parent = parent;
    }

    public abstract Plan buildPlan();

    @Override
    public int compareTo(AStarNode other) {
        return Float.compare(g + h, other.g + other.h);
    }

    @Override
    public abstract boolean equals(Object other);

    public abstract Iterable<AStarNode> getNeighbours();

    protected AStarNode getParent() {
        return parent;
    }

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

    protected float getPathCost() {
        return g + h;
    }

    @Override
    public abstract int hashCode();
}
