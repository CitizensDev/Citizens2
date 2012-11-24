package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Plan;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class LocationNode extends AStarNode {
    private float blockCost = -1;
    final BlockSource blockSource;
    private final BlockExaminer[] examiners;
    final Vector location;

    public LocationNode(Location location, BlockSource source, BlockExaminer... examiners) {
        this(location.toVector(), source, examiners);
    }

    private LocationNode(Vector location, BlockSource source, BlockExaminer... examiners) {
        this.location = location;
        this.blockSource = source;
        this.examiners = examiners == null ? new BlockExaminer[] {} : examiners;
    }

    boolean at(Vector goal) {
        return heuristicDistance(goal) < 1;
    }

    @Override
    public Plan buildPlan() {
        Iterable<LocationNode> parents = getParents();
        return new LocationPlan(parents);
    }

    public float distance(LocationNode to) {
        return (float) location.distanceSquared(to.location);
    }

    private float getBlockCost() {
        if (blockCost == -1) {
            blockCost = 0;
            for (BlockExaminer examiner : examiners) {
                blockCost += examiner.getCost(blockSource, location);
            }
        }
        return blockCost;
    }

    @Override
    public Iterable<AStarNode> getNeighbours() {
        List<AStarNode> nodes = Lists.newArrayList();
        for (BlockFace face : BlockFace.values()) {
            Vector mod = location.clone().add(new Vector(face.getModX(), face.getModY(), face.getModZ()));
            if (mod.equals(location))
                continue;
            if (!isPassable(mod))
                continue;
            nodes.add(getNewNode(mod));
        }
        return nodes;
    }

    private AStarNode getNewNode(Vector mod) {
        return new LocationNode(mod, blockSource, examiners);
    }

    public float heuristicDistance(Vector goal) {
        return (float) location.distanceSquared(goal) + getBlockCost();
    }

    private boolean isPassable(Vector mod) {
        for (BlockExaminer examiner : examiners)
            if (!examiner.isPassable(blockSource, mod))
                return false;
        return true;
    }
}