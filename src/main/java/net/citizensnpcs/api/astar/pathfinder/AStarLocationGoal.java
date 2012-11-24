package net.citizensnpcs.api.astar.pathfinder;


import net.citizensnpcs.api.astar.AStarGoal;
import net.citizensnpcs.api.astar.AStarNode;

import org.bukkit.Location;
import org.bukkit.util.Vector;


public class AStarLocationGoal implements AStarGoal {
    private final Vector goal;

    public AStarLocationGoal(Location dest) {
        this(dest.toVector());
    }

    public AStarLocationGoal(Vector goal) {
        this.goal = goal;
    }

    @Override
    public float g(AStarNode from, AStarNode to) {
        return ((LocationNode) from).distance((LocationNode) to);
    }

    @Override
    public float getInitialCost(AStarNode node) {
        return ((LocationNode) node).heuristicDistance(goal);
    }

    @Override
    public float h(AStarNode from) {
        return ((LocationNode) from).heuristicDistance(goal);
    }

    @Override
    public boolean isFinished(AStarNode node) {
        return ((LocationNode) node).at(goal);
    }
}
