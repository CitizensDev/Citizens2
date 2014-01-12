package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.astar.AStarGoal;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorGoal implements AStarGoal<VectorNode> {
    final Vector goal;
    private final float leeway;

    double lowest = Double.MAX_VALUE;

    public VectorGoal(Location dest, float range) {
        this(dest.toVector(), range);
    }

    public VectorGoal(Vector goal, float range) {
        this.goal = goal.setX(goal.getBlockX()).setY(goal.getBlockY()).setZ(goal.getBlockZ());
        this.leeway = range;
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
        double distanceSquared = node.getVector().distanceSquared(goal);
        return goal.equals(node.location) || distanceSquared <= leeway;
    }
}
