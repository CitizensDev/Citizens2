package net.citizensnpcs.api.astar.pathfinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

public class Path implements Plan {
    private List<Block> blockList;
    private int index = 0;
    private final PathEntry[] path;

    public Path(Collection<Vector> vector) {
        this.path = Iterables.toArray(
                Iterables.transform(vector, input -> new PathEntry(input, Collections.<PathCallback> emptyList())),
                PathEntry.class);
    }

    Path(Iterable<VectorNode> unfiltered) {
        this.path = cull(unfiltered);
    }

    private PathEntry[] cull(Iterable<VectorNode> unfiltered) {
        // possibly expose cullability in an API
        List<PathEntry> path = Lists.newArrayList();
        for (VectorNode node : unfiltered) {
            for (Vector vector : node.getPathVectors()) {
                path.add(new PathEntry(vector, node.callbacks));
            }
        }
        return path.toArray(new PathEntry[path.size()]);
    }

    public List<Block> getBlocks(World world) {
        return Arrays.asList(path).stream()
                .map(p -> world.getBlockAt(p.vector.getBlockX(), p.vector.getBlockY(), p.vector.getBlockZ()))
                .collect(Collectors.toList());
    }

    public Vector getCurrentVector() {
        return path[index].vector;
    }

    public Iterable<Vector> getPath() {
        return Iterables.transform(Arrays.asList(path), input -> input.vector);
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
        if (isComplete()) {
            return;
        }
        path[index].onComplete((NPC) agent);
        ++index;
    }

    private class PathEntry {
        final List<PathCallback> callbacks;
        final Vector vector;

        private PathEntry(Vector vector, List<PathCallback> callbacks) {
            this.vector = vector;
            this.callbacks = callbacks;
        }

        public void onComplete(NPC npc) {
            if (callbacks == null)
                return;
            Block current = npc.getEntity().getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(),
                    vector.getBlockZ());
            for (PathCallback callback : callbacks) {
                callback.onReached(npc, current);
            }
        }

        public void run(final NPC npc) {
            if (callbacks == null)
                return;
            Block current = npc.getEntity().getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(),
                    vector.getBlockZ());
            for (PathCallback callback : callbacks) {
                if (blockList == null) {
                    blockList = Lists.transform(Arrays.asList(path), input -> npc.getEntity().getWorld()
                            .getBlockAt(input.vector.getBlockX(), input.vector.getBlockY(), input.vector.getBlockZ()));
                }
                callback.run(npc, current, blockList, index);
            }
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }
}