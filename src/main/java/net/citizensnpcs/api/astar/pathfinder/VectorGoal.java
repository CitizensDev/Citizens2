package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.astar.AStarGoal;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorGoal implements AStarGoal<VectorNode> {
    private final Vector goal;

    public VectorGoal(Location dest) {
        this(dest.toVector());
    }

    public VectorGoal(Vector goal) {
        this.goal = goal.setX(goal.getBlockX()).setY(goal.getBlockY()).setZ(goal.getBlockZ());
    }

    @Override
    public float g(VectorNode from, VectorNode to) {
        return from.distance(to);
    }

    @Override
    public float getInitialCost(VectorNode node) {
        return node.heuristicDistance(goal);
    }

    @Override
    public float h(VectorNode from) {
        return from.heuristicDistance(goal);
    }

    @Override
    public boolean isFinished(VectorNode node) {
        return node.at(goal);
    }
}
