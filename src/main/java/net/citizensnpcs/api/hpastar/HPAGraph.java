package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.Location;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import ch.ethz.globis.phtree.PhTreeSolid;
import ch.ethz.globis.phtree.PhTreeSolid.PhQueryS;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.util.Messaging;

public class HPAGraph {
    private final BlockSource blockSource;
    public List<List<HPACluster>> clusters = Lists.newArrayList();
    // TODO: y-clusters?
    // TODO: make nodes updateable properly
    private final int cx, cy, cz;
    private final List<PhTreeSolid<HPACluster>> phtrees = Lists.newArrayList();

    public HPAGraph(BlockSource blockSource, int cx, int cy, int cz) {
        this.blockSource = blockSource;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;

        while (clusters.size() <= MAX_DEPTH) {
            clusters.add(new ArrayList<HPACluster>());
            if (clusters.size() != phtrees.size()) {
                phtrees.add(PhTreeSolid.create(3));
            }
        }
    }

    public void addClusters(int x, int z) {
        int baseX = MAX_CLUSTER_SIZE * ((x - cx) / MAX_CLUSTER_SIZE) + cx;
        int baseZ = MAX_CLUSTER_SIZE * ((z - cz) / MAX_CLUSTER_SIZE) + cz;
        Messaging.log(baseX, baseZ);
        List<HPACluster> newClusters = new ArrayList<>();
        if (phtrees.size() == 0) {
            phtrees.add(PhTreeSolid.create(3));
        }
        PhTreeSolid<HPACluster> baseLevel = phtrees.get(0);

        // build clusters
        int clusterSize = BASE_CLUSTER_SIZE;
        for (int y = 0; y < 128; y++) {
            for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                    HPACluster cluster = new HPACluster(this, 0, clusterSize, baseX + ci, y, baseZ + cj);
                    if (!cluster.hasWalkableNodes())
                        continue;
                    newClusters.add(cluster);
                    baseLevel.put(
                            new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ }, new long[] {
                                    cluster.clusterX + clusterSize, cluster.clusterY, cluster.clusterZ + clusterSize },
                            cluster);
                    Messaging.log(cluster);
                }
            }
        }

        Multimap<HPACluster, HPACluster> neighbours = HashMultimap.create();
        for (HPACluster cluster : newClusters) {
            PhQueryS<HPACluster> q = baseLevel.queryIntersect(
                    new long[] { cluster.clusterX - clusterSize, cluster.clusterY - 1, cluster.clusterZ - clusterSize },
                    new long[] { cluster.clusterX + clusterSize, cluster.clusterY + 1,
                            cluster.clusterZ + clusterSize });
            while (q.hasNext()) {
                HPACluster neighbour = q.nextValue();
                if (neighbour == cluster || neighbours.get(cluster).contains(neighbour))
                    continue;

                // TODO: diagonal connections using length=sqrt(2)
                if (neighbour.clusterX - cluster.clusterX != 0 && neighbour.clusterZ - cluster.clusterZ != 0)
                    continue;
                int dx = neighbour.clusterX - cluster.clusterX;
                int dz = neighbour.clusterZ - cluster.clusterZ;
                Direction direction = null;
                if (dx > 0)
                    direction = Direction.EAST;
                else if (dx < 0)
                    direction = Direction.WEST;
                else if (dz > 0)
                    direction = Direction.NORTH;
                else if (dz < 0)
                    direction = Direction.SOUTH;
                if (direction == null)
                    continue;
                cluster.connect(neighbour, direction);
                neighbours.get(cluster).add(neighbour);
                neighbours.get(neighbour).add(cluster);
                Messaging.log("CONNECTED", cluster, neighbour);
            }
        }
        for (HPACluster cluster : newClusters) {
            cluster.connectIntra();
        }
        addClustersAtDepth(0, newClusters);
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            newClusters = new ArrayList<HPACluster>();
            clusterSize = (int) (BASE_CLUSTER_SIZE * Math.pow(2, depth));

            for (int y = 0; y < 128; y++) {
                for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                    for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                        HPACluster cluster = new HPACluster(this, depth, clusterSize, baseX + ci, y, baseZ + cj);
                        List<HPACluster> parentClusters = Lists.newArrayList(phtrees.get(depth - 1).queryInclude(
                                new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ },
                                new long[] { cluster.clusterX + clusterSize, cluster.clusterY,
                                        cluster.clusterZ + clusterSize }));
                        if (parentClusters.size() == 0)
                            continue;
                        cluster.buildFrom(parentClusters);
                        phtrees.get(depth).put(new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ },
                                new long[] { cluster.clusterX + clusterSize, cluster.clusterY,
                                        cluster.clusterZ + clusterSize },
                                cluster);
                        Messaging.log(cluster);
                        newClusters.add(cluster);
                    }
                }
            }

            addClustersAtDepth(depth, newClusters);
        }
    }

    public void addClustersAtDepth(int depth, List<HPACluster> other) {
        clusters.get(depth).addAll(other);
    }

    public Plan findPath(Location start, Location goal) {
        // insert into each layer
        List<HPACluster> clustersToClean = new ArrayList<HPACluster>();
        HPAGraphNode startNode = new HPAGraphNode(start.getBlockX(), start.getBlockY(), start.getBlockZ()),
                goalNode = new HPAGraphNode(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
        int i = 0;
        // TODO: verify that below insertion code works properly
        for (PhTreeSolid<HPACluster> phtree : phtrees) {
            PhQueryS<HPACluster> q = phtree.queryIntersect(
                    new long[] { start.getBlockX(), start.getBlockY(), start.getBlockZ() },
                    new long[] { start.getBlockX(), start.getBlockY(), start.getBlockZ() });
            HPACluster startCluster = q.hasNext() ? q.next() : null;
            q = phtree.queryIntersect(new long[] { goal.getBlockX(), goal.getBlockY(), goal.getBlockZ() },
                    new long[] { goal.getBlockX(), goal.getBlockY(), goal.getBlockZ() });
            HPACluster goalCluster = q.hasNext() ? q.next() : null;
            Messaging.log(i, startCluster, goalCluster);
            startCluster.insert(startNode); // TODO: don't need to pathfind for higher levels
            goalCluster.insert(goalNode);
            clustersToClean.add(startCluster);
            clustersToClean.add(goalCluster);
        }
        AStarSolution sln = pathfind(startNode, goalNode, 0);
        System.out.println(":" + start + "->" + goal + "@" + sln.cost);
        for (HPACluster cluster : clustersToClean) {
            cluster.remove(startNode, goalNode);
        }
        return new Path(sln.convertToVectors());
    }

    AStarSolution pathfind(HPAGraphNode start, HPAGraphNode dest, int level) {
        Map<ReversableAStarNode, Float> open = new HashMap<ReversableAStarNode, Float>();
        Map<ReversableAStarNode, Float> closed = new HashMap<ReversableAStarNode, Float>();
        Queue<ReversableAStarNode> frontier = new PriorityQueue<ReversableAStarNode>();
        ReversableAStarNode startNode = new HPAGraphAStarNode(start, null);
        frontier.add(startNode);
        open.put(startNode, startNode.g);
        while (!frontier.isEmpty()) {
            HPAGraphAStarNode node = (HPAGraphAStarNode) frontier.poll();
            List<HPAGraphEdge> edges = node.node.getEdges(level);
            for (HPAGraphEdge edge : edges) {
                if (edge.to.equals(dest)) {
                    return new AStarSolution(node.reconstructSolution(), node.g);
                }
            }
            if (start != node.node) {
                closed.put(node, node.g);
            }
            open.remove(node);
            for (HPAGraphEdge edge : edges) {
                HPAGraphAStarNode neighbour = new HPAGraphAStarNode(edge.to, edge);
                if (closed.containsKey(neighbour))
                    continue;
                neighbour.parent = node;
                neighbour.g = node.g + edge.weight;
                neighbour.h = (float) Math.sqrt(Math.pow(edge.to.x - dest.x, 2) + Math.pow(edge.to.z - dest.z, 2));
                if (open.containsKey(neighbour)) {
                    if (neighbour.g > open.get(neighbour))
                        continue;
                    frontier.remove(neighbour);
                }
                open.put(neighbour, neighbour.g);
                frontier.add(neighbour);
            }
        }
        return new AStarSolution(null, Float.POSITIVE_INFINITY);
    }

    public boolean walkable(int x, int y, int z) {
        if (y == 0) {
            return false;
        }
        return MinecraftBlockExaminer.canStandOn(blockSource.getWorld().getBlockAt(x, y - 1, z));
    }

    private static int BASE_CLUSTER_SIZE = (int) (2 * Math.pow(2, 3));
    private static int MAX_CLUSTER_SIZE = (int) (2 * Math.pow(2, 5));
    private static int MAX_DEPTH = 3;
}