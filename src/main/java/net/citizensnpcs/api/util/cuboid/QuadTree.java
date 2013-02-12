package net.citizensnpcs.api.util.cuboid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuadTree {
    QuadNode root;

    /**
     * Adds the {@link QuadCuboid} to the target node and fixes up the children's
     * list holder link
     * 
     * @param node
     *            The target node
     * @param cuboid
     *            The cuboid to add
     */
    private void addAndFixListHolders(QuadNode node, QuadCuboid cuboid) {
        node.cuboids.add(cuboid);
        // This isn't our first cuboid, so no fix needed
        if (node.cuboids.size() > 1) {
            return;
        }
        // Descend the tree. When we find a node with no cuboids it needs a new
        // list holder
        Deque<QuadNode> todo = new ArrayDeque<QuadNode>();
        todo.push(node);
        QuadNode current;
        do {
            current = todo.pop();
            for (QuadNode child : current.quads) {
                if (child == null) {
                    continue;
                }
                // If the child isn't holding a list of cuboids itself
                // (which would make it the list holder link for its children)
                // Then we need to fix it's children as well
                if (child.cuboids.size() == 0) {
                    todo.push(child);
                }
                child.nextListHolder = node;
            }
        } while (!todo.isEmpty());
    }

    private QuadNode ascendFirstSearch(QuadNode node, int x, int z) {
        while (node != null && (node.x > x || node.z > z || (node.x + node.size) < x || (node.z + node.size) < z)) {
            node = node.parent;
        }
        if (node == null)
            return null;
        return descendAndSearch(node, x, z);
    }

    private void beginTree(QuadCuboid cuboid) {
        int size = 128;
        int minSize = Math.abs(cuboid.lowCoords[0] - cuboid.highCoords[0]);
        int minSizeB = Math.abs(cuboid.lowCoords[2] - cuboid.highCoords[2]);
        if (minSize < minSizeB) {
            minSize = minSizeB;
        }
        while (size < minSize) {
            size = size << 1;
        }
        root = new QuadNode(cuboid.lowCoords[0], cuboid.lowCoords[2], size - 1, null);
    }

    /**
     * Returns -1 for too small, 0 for minimal, 1 for larger than needed
     * <p>
     * Fit is based on the larger side. Means more tests but consumes an order
     * of magnitude or less memory.
     * 
     * @param node
     * @param cuboid
     * @return
     */
    private int containerFit(QuadNode node, QuadCuboid cuboid) {
        int minSizeA = Math.abs(cuboid.lowCoords[0] - cuboid.highCoords[0]);
        int minSizeB = Math.abs(cuboid.lowCoords[2] - cuboid.highCoords[2]);
        int fitSize;
        if (minSizeA < minSizeB) {
            fitSize = minSizeB;
        } else {
            fitSize = minSizeA;
        }

        if (node.size < fitSize) {
            return -1;
        } else if (node.size == 1 || (node.size >> 1) < fitSize) {
            return 0;
        } else {
            return 1;
        }
    }

    private QuadNode descendAndCreate(QuadNode start, QuadCuboid cuboid) {
        QuadNode next = start;
        while (containerFit(next, cuboid) > 0) {
            int i = 0;
            int nX = 0;
            int nZ = 0;
            int half = (next.size >> 1);
            if (cuboid.lowCoords[0] > (next.x + half)) {
                i++;
                nX = half + 1;
            }
            if (cuboid.lowCoords[2] > (next.z + half)) {
                i += 2;
                nZ = half + 1;
            }
            if (next.quads[i] == null) {
                next.quads[i] = new QuadNode(next.x + nX, next.z + nZ, half, next);
            }
            next = next.quads[i];
        }
        return next;
    }

    private QuadNode descendAndSearch(QuadNode node, int x, int z) {
        QuadNode next = node;
        while (next != null) {
            node = next;
            int half = node.size >> 1;
            int i = 0;
            if (x > (node.x + half)) {
                i++;
            }
            if (z > (node.z + half)) {
                i += 2;
            }
            next = node.quads[i];
        }
        return node;
    }

    private QuadNode descendNoCreate(QuadNode start, QuadCuboid cuboid) {
        QuadNode next = start;
        while (containerFit(next, cuboid) > 0) {
            int i = 0;
            int nX = 0;
            int nZ = 0;
            int half = (next.size >> 1);
            if (cuboid.lowCoords[0] > (next.x + half)) {
                i++;
                nX = half + 1;
            }
            if (cuboid.lowCoords[2] > (next.z + half)) {
                i += 2;
                nZ = half + 1;
            }
            if (next.quads[i] == null) {
                next = new QuadNode(next.x + nX, next.z + nZ, half, next);
            } else {
                next = next.quads[i];
            }
        }
        return next;
    }

    public BookmarkedResult findOverlappingCuboids(int x, int y, int z) {
        return relatedSearch(null, x, y, z);
    }

    public BookmarkedResult findOverlappingCuboidsFromBookmark(BookmarkedResult bookmark, int x, int y, int z) {
        return relatedSearch(bookmark.bookmark, x, y, z);
    }

    /**
     * Oftentimes a node will overlap with the neighbors of a node Since we
     * always search for the next node based on the lower left we know that the
     * left and bottom will not go over the edge, leaving only the top, right,
     * and upper right possibilities need be regarded. Spits out a list of
     * cuboids that are fit for "insertion" although we just use them for the
     * search and actually attach the original cuboid. We also return the
     * remainder shard if we generated any others. At the other end we only
     * include a node if it's shard didn't re-shard. Keeps the tree search
     * spaces minimal.
     */
    private List<QuadCuboid> generateShards(QuadNode node, QuadCuboid cuboid) {
        List<QuadCuboid> shards = new ArrayList<QuadCuboid>(4);
        int top = node.z + node.size;
        int right = node.x + node.size;
        int tmp;

        // find a shard above if it exists
        if (top < cuboid.highCoords[2]) {
            // Find out if it extends past the top only or the right and top
            // Limit the "top" shard to only directly above the original node
            if (right < cuboid.highCoords[0]) {
                tmp = right;
            } else {
                tmp = cuboid.highCoords[0];
            }
            shards.add(new QuadCuboid(cuboid.lowCoords[0], 0, top + 1, tmp, 0, cuboid.highCoords[2]));
        }
        // Find a shard to the right
        if (right < cuboid.highCoords[0]) {
            // find if we extend past the top as well
            // Limit the "right" shard to only directly right
            if (top < cuboid.highCoords[2]) {
                tmp = top;
            } else {
                tmp = cuboid.highCoords[2];
            }
            shards.add(new QuadCuboid(right + 1, 0, cuboid.lowCoords[2], cuboid.highCoords[0], 0, tmp));
        }
        // Check for a top right shard
        if (right < cuboid.highCoords[0] && top < cuboid.highCoords[2]) {
            shards.add(new QuadCuboid(right + 1, 0, top + 1, cuboid.highCoords[0], 0, cuboid.highCoords[2]));
        }
        // include the remainder as a shard if we generated any others
        if (shards.size() > 0) {
            shards.add(new QuadCuboid(cuboid.lowCoords[0], 0, cuboid.lowCoords[2], right, 0, top));
        }
        return shards;
    }

    public List<QuadCuboid> getAllOverlapsWith(QuadCuboid cuboid) {
        if (root == null)
            return Collections.emptyList();

        // if this cuboid falls outside of the tree, we need to repot the tree
        // to
        // gain a wider perspective!
        if (!nodeFullyContainsCuboid(root, cuboid)) {
            repotTree(cuboid);
        }
        QuadNode node = root;
        node = descendNoCreate(node, cuboid);
        // Now that we have our target we potentially need to generate shards
        // and
        // target their nodes as well
        List<QuadNode> targets = getAllTargetsNoCreate(node, cuboid);
        Deque<QuadNode> children = new ArrayDeque<QuadNode>();
        Set<QuadCuboid> cuboids = new HashSet<QuadCuboid>(256);
        // Generous initial capacity for speed
        QuadNode childTarget;
        // Of note: adding all the cuboids to the set and then testing is faster
        // than testing as we go and potentially getting out faster
        // This is especially true when there is less likely to be an overlap
        // anyway
        for (QuadNode target : targets) {
            // Drill down to the children nodes to get the smaller cuboids
            // contained therein
            children.add(target);
            do {
                childTarget = children.pop();
                for (QuadNode child : childTarget.quads) {
                    if (child == null) {
                        continue;
                    }
                    children.push(child);
                    cuboids.addAll(child.cuboids);
                }
            } while (!children.isEmpty());
            // Then ascend backup and add the ones there
            while (target != null) {
                cuboids.addAll(target.cuboids);
                target = target.nextListHolder;
            }
        }
        List<QuadCuboid> overlaps = new ArrayList<QuadCuboid>();
        for (QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
                overlaps.add(pc);
            }
        }
        return overlaps;
    }

    // Finds all the nodes that a cuboid should reside in (handles sharding)
    private List<QuadNode> getAllTargets(QuadNode initialNode, QuadCuboid cuboid) {
        List<QuadNode> targets = new ArrayList<QuadNode>();
        // Generate the initial shards
        Deque<QuadCuboid> shards = new ArrayDeque<QuadCuboid>();
        shards.addAll(generateShards(initialNode, cuboid));

        QuadNode node;
        while (!shards.isEmpty()) {
            QuadCuboid shard = shards.pop();
            node = descendAndCreate(root, shard);
            List<QuadCuboid> newShards = generateShards(node, shard);
            // If no shards were made then this is is the bounding node for this
            // shard. Include it.
            if (newShards.size() == 0) {
                targets.add(node);
            } else {
                shards.addAll(newShards);
            }
        }

        // If the initial shard attempt turns out to not have had
        // to generate shards then we need to add the initial node
        if (targets.size() == 0) {
            targets.add(initialNode);
        }

        return targets;
    }

    // Finds all the nodes that a cuboid should reside in (handles sharding)
    private List<QuadNode> getAllTargetsNoCreate(QuadNode initialNode, QuadCuboid cuboid) {
        List<QuadNode> targets = new ArrayList<QuadNode>();
        // Generate the initial shards
        Deque<QuadCuboid> shards = new ArrayDeque<QuadCuboid>();
        shards.addAll(generateShards(initialNode, cuboid));

        QuadNode node;
        while (!shards.isEmpty()) {
            QuadCuboid shard = shards.pop();
            node = descendNoCreate(root, shard);
            List<QuadCuboid> newShards = generateShards(node, shard);
            // If no shards were made then this is is the bounding node for this
            // shard. Include it.
            if (newShards.size() == 0) {
                targets.add(node);
            } else {
                shards.addAll(newShards);
            }
        }

        // If the initial shard attempt turns out to not have had
        // to generate shards then we need to add the initial node
        if (targets.size() == 0) {
            targets.add(initialNode);
        }

        return targets;
    }

    private List<QuadCuboid> getMatchingCuboids(QuadNode target, int x, int y, int z) {
        List<QuadCuboid> matches = new ArrayList<QuadCuboid>();
        while (target != null) {
            for (QuadCuboid potential : target.cuboids) {
                if (potential.includesPoint(x, y, z)) {
                    matches.add(potential);
                }
            }
            target = target.nextListHolder;
        }
        return matches;
    }

    public void insert(QuadCuboid cuboid) {
        if (root == null) {
            beginTree(cuboid);
        }
        // if this cuboid falls outside of the tree, we need to repot the tree
        // to
        // gain a wider perspective!
        if (!nodeFullyContainsCuboid(root, cuboid)) {
            repotTree(cuboid);
        }
        QuadNode node = root;
        node = descendAndCreate(node, cuboid);
        // Now that we have our target we potentially need to generate shards
        // and
        // target their nodes as well
        List<QuadNode> targets = getAllTargets(node, cuboid);
        // Add the cuboid everywhere it belongs
        for (QuadNode target : targets) {
            addAndFixListHolders(target, cuboid);
        }
    }

    /**
     * Attempts to insert the node ONLY if there are no overlaps with existing
     * nodes
     * 
     * @param cuboid
     *            cuboid to insert
     * @return success or failure
     */
    public boolean insertIfNoOverlaps(QuadCuboid cuboid) {
        if (root == null) {
            insert(cuboid);
            return true;
        }
        // if this cuboid falls outside of the tree, we need to repot the tree
        // to
        // gain a wider perspective!
        if (!nodeFullyContainsCuboid(root, cuboid)) {
            repotTree(cuboid);
        }
        QuadNode node = root;
        node = descendAndCreate(node, cuboid);
        // Now that we have our target we potentially need to generate shards
        // and target their nodes as well
        List<QuadNode> targets = getAllTargets(node, cuboid);
        Deque<QuadNode> children = new ArrayDeque<QuadNode>();
        Set<QuadCuboid> cuboids = new HashSet<QuadCuboid>(256);
        // Generous initial capacity for speed
        QuadNode childTarget;
        // Of note: adding all the cuboids to the set and then testing is faster
        // than testing as we go and potentially getting out faster
        // This is especially true when there is less likely to be an overlap
        // anyway
        for (QuadNode target : targets) {
            // Drill down to the children nodes to get the smaller cuboids
            // contained therein
            children.add(target);
            do {
                childTarget = children.pop();
                for (QuadNode child : childTarget.quads) {
                    if (child == null) {
                        continue;
                    }
                    children.push(child);
                    cuboids.addAll(child.cuboids);
                }
            } while (!children.isEmpty());
            // Then ascend backup and add the ones there
            while (target != null) {
                cuboids.addAll(target.cuboids);
                target = target.nextListHolder;
            }
        }
        for (QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
                for (QuadNode target : targets) {
                    if (target.cuboids.size() == 0) {
                        pruneTree(node);
                    }
                }
                return false;
            }
        }
        // Add the cuboid everywhere it belongs
        for (QuadNode target : targets) {
            addAndFixListHolders(target, cuboid);
        }
        return true;
    }

    private boolean nodeFullyContainsCuboid(QuadNode node, QuadCuboid cuboid) {
        return node.x <= cuboid.lowCoords[0] && node.z <= cuboid.lowCoords[2]
                && (node.x + node.size) >= cuboid.highCoords[0] && (node.z + node.size) >= cuboid.highCoords[2];
    }

    public boolean overlapsExisting(QuadCuboid cuboid) {
        if (root == null) {
            return false;
        }
        // if this cuboid falls outside of the tree, we need to repot the tree
        // to
        // gain a wider perspective!
        if (!nodeFullyContainsCuboid(root, cuboid)) {
            repotTree(cuboid);
        }
        QuadNode node = root;
        node = descendNoCreate(node, cuboid);
        // Now that we have our target we potentially need to generate shards
        // and
        // target their nodes as well
        List<QuadNode> targets = getAllTargetsNoCreate(node, cuboid);
        Deque<QuadNode> children = new ArrayDeque<QuadNode>();
        Set<QuadCuboid> cuboids = new HashSet<QuadCuboid>(256);
        // Generous initial capacity for speed
        QuadNode childTarget;
        // Of note: adding all the cuboids to the set and then testing is faster
        // than testing as we go and potentially getting out faster
        // This is especially true when there is less likely to be an overlap
        // anyway
        for (QuadNode target : targets) {
            // Drill down to the children nodes to get the smaller cuboids
            // contained therein
            children.add(target);
            do {
                childTarget = children.pop();
                for (QuadNode child : childTarget.quads) {
                    if (child == null) {
                        continue;
                    }
                    children.push(child);
                    cuboids.addAll(child.cuboids);
                }
            } while (!children.isEmpty());
            // Then ascend backup and add the ones there
            while (target != null) {
                cuboids.addAll(target.cuboids);
                target = target.nextListHolder;
            }
        }
        for (QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes any node from the tree that no longer serves a purpose, starting
     * from the node given and moving up
     * 
     * @param node
     */
    private void pruneTree(QuadNode node) {
        int i;
        while (node.parent != null && node.quads[0] == null && node.quads[1] == null && node.quads[2] == null
                && node.quads[3] == null) {
            i = 0;
            if (node.x != node.parent.x)
                i++;

            if (node.z != node.parent.z)
                i += 2;

            node = node.parent;
            node.quads[i] = null;
        }
    }

    private BookmarkedResult relatedSearch(QuadNode bookmark, int x, int y, int z) {
        if (bookmark == null)
            bookmark = root;

        QuadNode node = ascendFirstSearch(bookmark, x, z);
        return new BookmarkedResult(node, getMatchingCuboids(node, x, y, z));
    }

    public void remove(QuadCuboid cuboid) {
        // No root? No-Op!
        if (root == null) {
            return;
        }
        QuadNode node;
        // Should not create any new nodes, but only if the cuboid is, in fact,
        // in
        // the tree
        node = descendAndCreate(root, cuboid);
        // Using the same algorithm that was used during creation will give us
        // the
        // same list of nodes to examine
        List<QuadNode> targets = getAllTargets(node, cuboid);
        for (QuadNode target : targets) {
            removeAndFixListHolders(target, cuboid);
        }
    }

    private void removeAndFixListHolders(QuadNode node, QuadCuboid cuboid) {
        node.cuboids.remove(cuboid);
        // This wasn't our only cuboid, so no fix needed
        if (node.cuboids.size() > 0)
            return;

        // Descend the tree. When we find a node with no children we know it
        // needs a new list holder
        Deque<QuadNode> todo = new ArrayDeque<QuadNode>();
        todo.push(node);
        QuadNode current;
        do {
            current = todo.pop();
            for (QuadNode child : current.quads) {
                if (child == null) {
                    continue;
                }
                // If the child isn't holding a list of cuboids itself
                // (which would make it the list holder link for its children)
                // Then we need to fix its children as well
                if (child.cuboids.size() == 0)
                    todo.push(child);

                child.nextListHolder = node.nextListHolder;
            }
        } while (!todo.isEmpty());
        pruneTree(node);
    }

    /**
     * Grow the tree beyond the root in the direction of the target node
     * 
     * @param cuboid
     */
    private void repotTree(QuadCuboid cuboid) {
        QuadNode oldRoot;
        int i;
        do {
            oldRoot = root;
            root = new QuadNode(oldRoot.x, oldRoot.z, (oldRoot.size << 1) + 1, null);
            oldRoot.parent = root;
            // Figure out the best direction to grow in (that is, which quadrant
            // is the old root in the new root?)
            // We start at lower left (quad 0)
            i = 0;
            // The target is left of us
            if (cuboid.lowCoords[0] < root.x) {
                i++;
                root.x -= oldRoot.size + 1;
            }
            // The target is below us
            if (cuboid.lowCoords[2] < root.z) {
                i += 2;
                root.z -= oldRoot.size + 1;
            }
            root.quads[i] = oldRoot;
        } while (!nodeFullyContainsCuboid(root, cuboid));
    }

    public List<QuadCuboid> search(int x, int y, int z) {
        QuadNode node = descendAndSearch(root, x, z);
        return getMatchingCuboids(node, x, y, z);
    }
}
