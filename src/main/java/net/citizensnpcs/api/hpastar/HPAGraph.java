package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;

import com.google.common.collect.Lists;

import ch.ethz.globis.phtree.PhTreeSolid;
import ch.ethz.globis.phtree.PhTreeSolid.PhQueryS;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;

public class HPAGraph {
    private final BlockSource blockSource;
    public List<List<HPACluster>> clusters = Lists.newArrayList();
    // TODO: y-clusters
    // TODO: make nodes updateable properly
    private final int cx, cy, cz;
    private final List<PhTreeSolid<HPACluster>> phtrees = Lists.newArrayList();

    public HPAGraph(BlockSource blockSource, int cx, int cy, int cz) {
        this.blockSource = blockSource;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }

    public void addClusters(int x, int z) {
        int baseX = MAX_CLUSTER_SIZE * ((x - cx) / MAX_CLUSTER_SIZE) + cx;
        int baseZ = MAX_CLUSTER_SIZE * ((z - cz) / MAX_CLUSTER_SIZE) + cz;
        List<HPACluster> newClusters = new ArrayList<>();
        PhTreeSolid<HPACluster> baseTree = phtrees.get(0);

        // build clusters
        int clusterSize = BASE_CLUSTER_SIZE / 2;
        for (int y = 0; y < 128; y++) {
            for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                    HPACluster cluster = new HPACluster(this, 0, clusterSize, y, baseX + ci, baseZ + cj);
                    newClusters.add(cluster);
                    baseTree.put(
                            new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ }, new long[] {
                                    cluster.clusterX + clusterSize, cluster.clusterY, cluster.clusterZ + clusterSize },
                            cluster);
                }
            }
        }

        Map<HPACluster, List<HPACluster>> clusterMap = new IdentityHashMap<>();
        for (HPACluster cluster : newClusters) {
            PhQueryS<HPACluster> q = baseTree.queryIntersect(
                    new long[] { cluster.clusterX - clusterSize, cluster.clusterY, cluster.clusterZ - clusterSize },
                    new long[] { cluster.clusterX + clusterSize, cluster.clusterY, cluster.clusterZ + clusterSize });
            while (q.hasNext()) {
                HPACluster neighbour = q.nextValue();
                if (neighbour == cluster || clusterMap.get(cluster).contains(neighbour))
                    continue;
                // TODO: diagonal connections using length=sqrt(2)
                if (neighbour.clusterX - cluster.clusterX != 0 && neighbour.clusterZ - cluster.clusterZ != 0)
                    continue;
                int dx = neighbour.clusterX - cluster.clusterX;
                int dz = neighbour.clusterZ - cluster.clusterZ;
                Direction direction = null;
                if (dx > 0)
                    direction = Direction.EAST;
                if (dx < 0)
                    direction = Direction.WEST;
                if (dz > 0)
                    direction = Direction.NORTH;
                if (dz < 0)
                    direction = Direction.SOUTH;
                cluster.connect(neighbour, direction);
                clusterMap.putIfAbsent(cluster, new ArrayList<HPACluster>());
                clusterMap.putIfAbsent(neighbour, new ArrayList<HPACluster>());
                clusterMap.get(cluster).add(neighbour);
                clusterMap.get(neighbour).add(cluster);
            }
        }
        for (HPACluster cluster : newClusters) {
            cluster.connectIntra();
        }
        addClustersAtDepth(0, newClusters);

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            newClusters = new ArrayList<HPACluster>();
            clusterSize = (int) (2 * Math.pow(2, depth));

            for (int y = 0; y < 128; y++) {
                for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                    for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                        HPACluster cluster = new HPACluster(this, depth, clusterSize, y, baseX + ci, baseZ + cj);
                        PhTreeSolid<HPACluster> lowerDepth = this.phtrees.get(depth - 1);
                        cluster.buildFrom(Lists.newArrayList(lowerDepth.queryInclude(
                                new long[] { cluster.clusterX - clusterSize, cluster.clusterY,
                                        cluster.clusterZ - clusterSize },
                                new long[] { cluster.clusterX + clusterSize, cluster.clusterY,
                                        cluster.clusterZ + clusterSize })));
                        newClusters.add(cluster);
                    }
                }
            }

            addClustersAtDepth(depth, newClusters);
        }
    }

    public void addClustersAtDepth(int depth, List<HPACluster> other) {
        while (clusters.size() <= depth) {
            clusters.add(new ArrayList<HPACluster>());
            phtrees.add(PhTreeSolid.<HPACluster> create(3));
        }
        clusters.get(depth).addAll(other);
    }

    public Plan findPath(Location start, Location goal) {
        // insert into each layer
        List<HPACluster> clustersToClean = new ArrayList<HPACluster>();
        HPAGraphNode startNode = new HPAGraphNode(start.getBlockX(), start.getBlockY(), start.getBlockZ()),
                goalNode = new HPAGraphNode(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
        for (PhTreeSolid<HPACluster> phtree : phtrees) {
            HPACluster startCluster = phtree.get(new long[] { start.getBlockX(), start.getBlockY(), start.getBlockZ() },
                    new long[] { start.getBlockX(), start.getBlockY(), start.getBlockZ() });
            HPACluster goalCluster = phtree.get(new long[] { goal.getBlockX(), goal.getBlockY(), goal.getBlockZ() },
                    new long[] { goal.getBlockX(), goal.getBlockY(), goal.getBlockZ() });
            startCluster.insert(startNode);
            goalCluster.insert(goalNode);
            clustersToClean.add(startCluster);
            clustersToClean.add(goalCluster);
        }
        AStarSolution sln = pathfind(startNode, goalNode, 0);
        System.out.println(":" + start + "->" + goal + "=" + sln.cost);
        for (HPACluster cluster : clustersToClean) {
            cluster.remove(startNode, goalNode);
        }
        return new Path(sln.convertToVectors());
    }

    AStarSolution pathfind(HPAGraphNode start, HPAGraphNode dest, int level) {
        Map<SimpleAStarNode, Float> open = new HashMap<SimpleAStarNode, Float>();
        Map<SimpleAStarNode, Float> closed = new HashMap<SimpleAStarNode, Float>();
        Queue<SimpleAStarNode> frontier = new PriorityQueue<SimpleAStarNode>();
        SimpleAStarNode startNode = new HPAGraphAStarNode(start, null);
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
        Material in = blockSource.getMaterialAt(x, y, z), on = blockSource.getMaterialAt(x, y - 1, z),
                above = blockSource.getMaterialAt(x, y + 2, z);
        return MinecraftBlockExaminer.canStandOn(in) && MinecraftBlockExaminer.canStandIn(on)
                && MinecraftBlockExaminer.canStandIn(above);
    }

    private static int BASE_CLUSTER_SIZE = (int) (2 * Math.pow(2, 1));
    private static int MAX_CLUSTER_SIZE = (int) (2 * Math.pow(2, 7));
    private static int MAX_DEPTH = 3;
}