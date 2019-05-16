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

import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;

public class HPAGraph {
    // TODO: y-clusters
    private final BlockSource blockSource;
    public List<List<HPACluster>> clusters = Lists.newArrayList();

    public HPAGraph(BlockSource blockSource) {
        this.blockSource = blockSource;
    }

    public void addClustersAtDepth(int depth, List<HPACluster> other) {
        while (clusters.size() <= depth) {
            clusters.add(new ArrayList<HPACluster>());
        }
        clusters.get(depth).addAll(other);
    }

    public void buildClusters(Tile[][][] tiles, int depth) {
        // TODO: convert this to flood-fill
        int clusterSize = (int) (2 * Math.pow(2, depth));
        HPACluster[][][] clusters = new HPACluster[16][16 / clusterSize][16 / clusterSize];
        if (depth > 0) {
            List<HPACluster> newClusters = new ArrayList<>();
            for (int y = 0; y < tiles.length; y++) {
                for (int ci = 0; ci < 16; ci += clusterSize) {
                    for (int cj = 0; cj < 16; cj += clusterSize) {
                        HPACluster cluster = new HPACluster(this, depth, clusterSize, y, ci, cj);
                        List<HPACluster> subClusters = new ArrayList<>();
                        for (HPACluster other : this.clusters.get(depth - 1)) {
                            if (cluster.contains(other)) {
                                subClusters.add(other);
                            }
                        }
                        cluster.buildFrom(subClusters);
                        newClusters.add(cluster);
                    }
                }
            }
            addClustersAtDepth(depth, newClusters);
            return;
        }
        // build clusters
        for (int y = 0; y < tiles.length; y++) {
            Tile[][] ylevel = tiles[y];
            for (int ci = 0; ci < 16; ci += clusterSize) {
                for (int cj = 0; cj < 16; cj += clusterSize) {
                    HPACluster cluster = new HPACluster(this, depth, clusterSize, y, ci, cj);
                    boolean add = false;
                    /* for (int j = 0; j < clusterSize; j++) {
                        for (int k = 0; k < clusterSize; k++) {
                            Tile in = ylevel[ci + j][cj + k];
                            if (in.y < 14) { // TODO
                                Tile on = tiles[in.y + 1][ci + j][cj + k];
                                Tile above = tiles[in.y + 2][ci + j][cj + k];
                                if (isWalkable(in.type, on.type, above.type)) {
                                    in.cluster = cluster;
                                    add = true;
                                }
                            }
                        }
                    }*/
                    if (add || true) {
                        clusters[y][ci / clusterSize][cj / clusterSize] = cluster;
                    } // TODO: can this optimisation be done
                }
            }
        }
        // build nodes
        List<HPACluster> clusterList = new ArrayList<HPACluster>();
        Map<HPACluster, List<HPACluster>> clusterMap = new IdentityHashMap<>();
        int[][] moves = { { 0, 1 }, { 1, 0 }, { -1, 0 }, { 0, -1 } };

        // TODO: diagonal connections using length=sqrt(2)
        for (int y = 0; y < clusters.length; y++) {
            for (int x = 0; x < 16 / clusterSize; x++) {
                for (int z = 0; z < 16 / clusterSize; z++) {
                    HPACluster base = clusters[y][x][z];
                    if (base == null)
                        continue;
                    clusterList.add(base);
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int[] move : moves) {
                            int dx = move[0], dz = move[1];
                            if (y + dy < 0 || x + dx < 0 || z + dz < 0 || y + dy >= 16 || x + dx >= 16 / clusterSize
                                    || z + dz >= 16 / clusterSize)
                                continue;
                            HPACluster other = clusters[y + dy][x + dx][z + dz];
                            if (other == null)
                                continue;
                            if (clusterMap.containsKey(base) && clusterMap.get(base).contains(other)) {
                                continue;
                            }
                            Direction direction = null;
                            if (dx > 0)
                                direction = Direction.EAST;
                            if (dx < 0)
                                direction = Direction.WEST;
                            if (dz > 0)
                                direction = Direction.NORTH;
                            if (dz < 0)
                                direction = Direction.SOUTH;
                            base.connect(other, direction);

                            clusterMap.putIfAbsent(base, new ArrayList<HPACluster>());
                            clusterMap.putIfAbsent(other, new ArrayList<HPACluster>());
                            clusterMap.get(base).add(other);
                            clusterMap.get(other).add(base);
                        }
                    }
                }
            }
        }
        for (HPACluster cluster : clusterList) {
            cluster.connectIntra();
        }
        addClustersAtDepth(depth, clusterList);
    }

    private double dist(Location start, HPACluster cluster) {
        return Math.sqrt(Math.pow(start.getBlockX() - (cluster.clusterX), 2)
                + Math.pow(start.getBlockZ() - (cluster.clusterZ), 2)
                + Math.pow(start.getBlockY() - cluster.clusterY, 2));
    }

    public Plan findPath(Location start, Location goal) {
        // insert into each layer
        List<HPACluster> clustersToClean = new ArrayList<HPACluster>();
        HPAGraphNode startNode = new HPAGraphNode(start.getBlockX(), start.getBlockY(), start.getBlockZ()),
                goalNode = new HPAGraphNode(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
        for (List<HPACluster> clusterLayer : clusters) {
            double minDistStart = Double.MAX_VALUE, minDistGoal = Double.MAX_VALUE;
            HPACluster startCluster = null, goalCluster = null;
            // TODO: optimise this
            for (HPACluster cluster : clusterLayer) {
                double distStart = dist(start, cluster);
                double distGoal = dist(goal, cluster);
                if (minDistStart > distStart) {
                    startCluster = cluster;
                    minDistStart = distStart;

                }
                if (minDistGoal > distGoal) {
                    goalCluster = cluster;
                    minDistGoal = distGoal;
                }
            }
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
}