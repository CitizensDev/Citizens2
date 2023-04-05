package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.AStarGoal;

public class VectorGoal implements AStarGoal<VectorNode> {
    private final Vector goal;
    private final float leeway;

    public VectorGoal(Location dest, float range) {
        if (!MinecraftBlockExaminer.canStandIn(dest.getBlock().getType())) {
            dest = MinecraftBlockExaminer.findValidLocationAbove(dest, 2);
        }
        this.leeway = range;
        this.goal = dest.toVector();
        goal.setX(goal.getBlockX()).setY(goal.getBlockY()).setZ(goal.getBlockZ());
    }

    @Override
    public float g(VectorNode from, VectorNode to) {
        return from.distance(to);
    }

    public Vector getGoalVector() {
        return goal.clone();
    }

    @Override
    public float getInitialCost(VectorNode node) {
        return (float) node.getVector().distance(goal);
    }

    @Override
    public float h(VectorNode from) {
        return from.heuristicDistance(goal);
    }

    @Override
    public boolean isFinished(VectorNode node) {
        double distance = node.location.distance(goal);
        return goal.equals(node.location) || distance <= leeway;
    }
}
