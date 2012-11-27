package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.astar.AStarGoal;
import net.citizensnpcs.api.astar.AStarNode;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorGoal implements AStarGoal {
    private final Vector goal;

    public VectorGoal(Location dest) {
        this(dest.toVector());
    }

    public VectorGoal(Vector goal) {
        this.goal = goal;
    }

    @Override
    public float g(AStarNode from, AStarNode to) {
        return ((VectorNode) from).distance((VectorNode) to);
    }

    @Override
    public float getInitialCost(AStarNode node) {
        return ((VectorNode) node).heuristicDistance(goal);
    }

    @Override
    public float h(AStarNode from) {
        return ((VectorNode) from).heuristicDistance(goal);
    }

    @Override
    public boolean isFinished(AStarNode node) {
        return ((VectorNode) node).at(goal);
    }
}
