package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.PassableState;
import net.citizensnpcs.api.util.SpigotUtil;

public class VectorNode extends AStarNode implements PathPoint {
    private float blockCost = -1;
    List<PathCallback> callbacks;
    private final PathInfo info;
    Vector location;
    Vector locationCache;
    List<Vector> pathVectors;

    public VectorNode(VectorGoal goal, Location location, BlockSource source, BlockExaminer... examiners) {
        this(null, goal, location.toVector(), source, examiners);
    }

    public VectorNode(VectorNode parent, Vector location, PathInfo info) {
        super(parent);
        this.location = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.info = info;
    }

    public VectorNode(VectorNode parent, VectorGoal goal, Vector location, BlockSource source,
            BlockExaminer... examiners) {
        this(parent, location, new PathInfo(source, examiners == null ? EMPTY_BLOCK_EXAMINER : examiners, goal));
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
        return new Path(getParents());
    }

    @Override
    public VectorNode createAtOffset(Vector mod) {
        return new VectorNode(this, mod, info);
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
            for (BlockExaminer examiner : info.examiners) {
                blockCost += examiner.getCost(info.blockSource, this);
            }
        }
        return blockCost;
    }

    @Override
    public Vector getGoal() {
        return info.goal.getGoalVector();
    }

    @Override
    public Iterable<AStarNode> getNeighbours() {
        List<PathPoint> neighbours = null;
        for (BlockExaminer examiner : info.examiners) {
            if (examiner instanceof NeighbourGeneratorBlockExaminer) {
                neighbours = ((NeighbourGeneratorBlockExaminer) examiner).getNeighbours(info.blockSource, this);
                break;
            }
        }
        if (neighbours == null) {
            neighbours = getNeighbours(info.blockSource, this);
        }
        List<AStarNode> nodes = Lists.newArrayList();
        for (PathPoint sub : neighbours) {
            if (!isPassable(sub))
                continue;
            nodes.add((AStarNode) sub);
        }
        return nodes;
    }

    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        return getNeighbours(source, point, true);
    }

    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point, boolean checkPassable) {
        List<PathPoint> neighbours = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    int modY = location.getBlockY() + y;
                    if (!SpigotUtil.checkYSafe(modY, source.getWorld())) {
                        continue;
                    }
                    Vector mod = new Vector(location.getX() + x, modY, location.getZ() + z);
                    if (mod.equals(location))
                        continue;
                    if (x != 0 && z != 0 && checkPassable) {
                        if (!isPassable(point.createAtOffset(new Vector(location.getX() + x, modY, location.getZ())))
                                || !isPassable(
                                        point.createAtOffset(new Vector(location.getX(), modY, location.getZ() + z)))) {
                            continue;
                        }
                    }
                    neighbours.add(point.createAtOffset(mod));
                }
            }
        }
        return neighbours;
    }

    @Override
    public PathPoint getParentPoint() {
        return (PathPoint) getParent();
    }

    @Override
    public List<Vector> getPathVectors() {
        return pathVectors != null ? pathVectors : ImmutableList.of(location);
    }

    @Override
    public Vector getVector() {
        if (locationCache == null) {
            locationCache = location.clone();
        }
        return locationCache.setX(location.getBlockX()).setY(location.getBlockY()).setZ(location.getBlockZ());
    }

    @Override
    public int hashCode() {
        return 31 + ((location == null) ? 0 : location.hashCode());
    }

    public float heuristicDistance(Vector goal) {
        return (float) (location.distance(goal) + getBlockCost()) * TIEBREAKER;
    }

    private boolean isPassable(PathPoint mod) {
        boolean passable = false;
        for (BlockExaminer examiner : info.examiners) {
            PassableState state = examiner.isPassable(info.blockSource, mod);
            if (state == PassableState.IGNORE)
                continue;
            passable = state == PassableState.PASSABLE ? true : false;
        }
        return passable;
    }

    @Override
    public void setPathVectors(List<Vector> vectors) {
        this.pathVectors = vectors;
    }

    @Override
    public void setVector(Vector vector) {
        this.location = vector;
    }

    private static class PathInfo {
        private final BlockSource blockSource;
        private final BlockExaminer[] examiners;
        private final VectorGoal goal;

        private PathInfo(BlockSource source, BlockExaminer[] examiners, VectorGoal goal) {
            this.blockSource = source;
            this.examiners = examiners;
            this.goal = goal;
        }
    }

    private static final BlockExaminer[] EMPTY_BLOCK_EXAMINER = new BlockExaminer[] {};
    private static final float TIEBREAKER = 1.001f;
}