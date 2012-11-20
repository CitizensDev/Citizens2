package net.citizensnpcs.api.astar;

import java.util.List;
import java.util.Set;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LocationGoal implements AStarGoal {
    private final Vector goal;

    public LocationGoal(Vector goal) {
        this.goal = goal;
    }

    @Override
    public float g(AStarNode from, AStarNode to) {
        return ((LocationNode) from).distance((LocationNode) to);
    }

    @Override
    public float getInitialCost(AStarNode node) {
        return ((LocationNode) node).distance(goal);
    }

    @Override
    public float h(AStarNode from) {
        return ((LocationNode) from).distance(goal);
    }

    @Override
    public boolean isFinished(AStarNode node) {
        return ((LocationNode) node).at(goal);
    }

    public static class LocationPlan implements Plan {

        @Override
        public int compareTo(Plan o) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isComplete() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void update() {
            // TODO Auto-generated method stub

        }
    }

    public static class LocationNode extends AStarNode {
        private final Vector location;
        private final BlockSource blockSource;

        public LocationNode(Vector location, BlockSource source) {
            this.location = location;
            this.blockSource = source;
        }

        public float distance(LocationNode to) {
            return distance(to.location);
        }

        public float distance(Vector goal) {
            return (float) location.distanceSquared(goal);
        }

        public boolean at(Vector goal) {
            return distance(goal) < 1;
        }

        @Override
        public Plan buildPlan() {
            Iterable<LocationNode> parents = getParents();
            for (LocationNode parent : parents) {
                System.err.println(parent.location);
            }
            return null;
        }

        @Override
        public Iterable<AStarNode> getNeighbours() {
            List<AStarNode> nodes = Lists.newArrayList();
            Set<Vector> mods = Sets.newHashSet();
            for (BlockFace face : BlockFace.values()) {
                Vector mod = location.clone().add(new Vector(face.getModX(), face.getModY(), face.getModZ()));
                if (mods.contains(mod) || mod.equals(location))
                    continue;
                mods.add(mod);
                nodes.add(new LocationNode(mod, blockSource));
            }
            return nodes;
        }
    }

    public static interface BlockSource {
        int getBlockTypeIdAt(int x, int y, int z);
    }
}
