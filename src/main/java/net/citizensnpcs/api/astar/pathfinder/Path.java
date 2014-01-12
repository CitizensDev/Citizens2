package net.citizensnpcs.api.astar.pathfinder;

import java.util.Arrays;
import java.util.List;

import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class Path implements Plan {
    private int index = 0;
    private final PathEntry[] path;

    Path(Iterable<VectorNode> unfiltered) {
        this.path = cull(unfiltered);
    }

    private PathEntry[] cull(Iterable<VectorNode> unfiltered) {
        // possibly expose cullability in an API
        List<PathEntry> path = Lists.newArrayList();
        for (VectorNode node : unfiltered) {
            if (node.callbacks != null)
                continue;
            Vector vector = node.location;
            path.add(new PathEntry(vector, node.callbacks));
        }
        return path.toArray(new PathEntry[path.size()]);
    }

    public Vector getCurrentVector() {
        return path[index].vector;
    }

    @Override
    public boolean isComplete() {
        return index >= path.length;
    }

    public void run(NPC npc) {
        path[index].run(npc);
    }

    @Override
    public String toString() {
        return Arrays.toString(path);
    }

    @Override
    public void update(Agent agent) {
        if (isComplete())
            return;
        ++index;
    }

    private static class PathEntry {
        final List<PathCallback> callbacks;
        final Vector vector;

        private PathEntry(Vector vector, List<PathCallback> callbacks) {
            this.vector = vector;
            this.callbacks = callbacks;
        }

        private Block getBlockUsingWorld(World world) {
            return world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        }

        public void run(NPC npc) {
            if (callbacks != null) {
                Block block = getBlockUsingWorld(npc.getEntity().getWorld());
                double distance = npc.getStoredLocation().distance(block.getLocation());
                for (PathCallback callback : callbacks) {
                    callback.run(npc, block, distance);
                }
            }
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }
}