package net.citizensnpcs.api.astar;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

/**
 * A base implementation of {@link AStarStorage} that uses a {@link PriorityQueue} for the frontier and {@link HashMap}s
 * for the open/closed sets.
 */
public class SimpleAStarStorage implements AStarStorage {
    private final Map<AStarNode, Float> closed = Maps.newHashMapWithExpectedSize(512);
    private final Map<AStarNode, Float> open = Maps.newHashMapWithExpectedSize(128);
    private final Queue<AStarNode> queue = new PriorityQueue<AStarNode>(128);

    @Override
    public void close(AStarNode node) {
        open.remove(node);
        closed.put(node, node.g);
    }

    @Override
    public AStarNode getBestNode() {
        return queue.peek();
    }

    @Override
    public void open(AStarNode node) {
        queue.offer(node);
        open.put(node, node.g);
        closed.remove(node);
    }

    @Override
    public AStarNode removeBestNode() {
        return queue.poll();
    }

    @Override
    public boolean shouldExamine(AStarNode neighbour) {
        Float openG = open.get(neighbour);
        if (openG != null && openG > neighbour.g) {
            open.remove(neighbour);
            openG = null;
        }
        Float closedG = closed.get(neighbour);
        if (closedG != null && closedG > neighbour.g) {
            closed.remove(neighbour);
            closedG = null;
        }
        return closedG == null && openG == null;
    }

    @Override
    public String toString() {
        return "SimpleAStarStorage [closed=" + closed + ", open=" + open + "]";
    }

    public static final Supplier<AStarStorage> FACTORY = new Supplier<AStarStorage>() {
        @Override
        public AStarStorage get() {
            return new SimpleAStarStorage();
        }
    };
}
