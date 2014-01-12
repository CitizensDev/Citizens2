package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.PassableState;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class VectorNode extends AStarNode implements PathPoint {
    private float blockCost = -1;
    private final BlockSource blockSource;
    List<PathCallback> callbacks;
    private final BlockExaminer[] examiners;
    private final VectorGoal goal;
    Vector location;

    public VectorNode(VectorGoal goal, Location location, BlockSource source, BlockExaminer... examiners) {
        this(goal, location.toVector(), source, examiners);
    }

    public VectorNode(VectorGoal goal, Vector location, BlockSource source, BlockExaminer... examiners) {
        this.location = location.setX(location.getBlockX()).setY(location.getBlockY()).setZ(location.getBlockZ());
        this.blockSource = source;
        this.examiners = examiners == null ? new BlockExaminer[] {} : examiners;
        this.goal = goal;
    }

    @Override
    public void addCallback(PathCallback callback) {
        if (callbacks == null) {
            callbacks = Lists.newArrayList();
        }
        callbacks.add(callback);
    }

    @Override
    public Plan buildPlan() {
        Iterable<VectorNode> parents = getParents();
        return new Path(parents);
    }

    public float distance(VectorNode to) {
        return (float) location.distance(to.location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VectorNode other = (VectorNode) obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }

    private float getBlockCost() {
        if (blockCost == -1) {
            blockCost = 0;
            for (BlockExaminer examiner : examiners) {
                blockCost += examiner.getCost(blockSource, this);
            }
        }
        return blockCost;
    }

    @Override
    public Vector getGoal() {
        return goal.goal;
    }

    @Override
    public Iterable<AStarNode> getNeighbours() {
        List<AStarNode> nodes = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (x != 0 && z != 0)
                        continue;
                    Vector mod = location.clone().add(new Vector(x, y, z));
                    if (mod.equals(location))
                        continue;
                    VectorNode sub = getNewNode(mod);
                    if (!isPassable(sub))
                        continue;
                    nodes.add(sub);
                }
            }
        }
        return nodes;
    }

    private VectorNode getNewNode(Vector mod) {
        return new VectorNode(goal, mod, blockSource, examiners);
    }

    @Override
    public PathPoint getParentPoint() {
        return (PathPoint) getParent();
    }

    @Override
    public Vector getVector() {
        return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((location == null) ? 0 : location.hashCode());
    }

    public float heuristicDistance(Vector goal) {
        return (float) (location.distance(goal) + getBlockCost()) * TIEBREAKER;
    }

    private boolean isPassable(PathPoint mod) {
        boolean passable = true;
        for (BlockExaminer examiner : examiners) {
            PassableState state = examiner.isPassable(blockSource, mod);
            if (state == PassableState.IGNORE)
                continue;
            passable = state == PassableState.PASSABLE ? true : false;
        }
        return passable;
    }

    @Override
    public void setVector(Vector vector) {
        this.location = vector;
    }

    private static final float TIEBREAKER = 1.001f;
}