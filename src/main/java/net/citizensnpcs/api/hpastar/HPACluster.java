package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Consumer;

public class HPACluster {
    private final int clusterSize;
    final int clusterX;
    final int clusterY;
    final int clusterZ;
    private final HPAGraph graph;
    private final int level;
    private final List<HPAGraphNode> nodes = new ArrayList<HPAGraphNode>();

    public HPACluster(HPAGraph graph, int level, int clusterSize, int clusterX, int clusterY, int clusterZ) {
        this.graph = graph;
        this.level = level;
        this.clusterSize = clusterSize;
        this.clusterX = clusterX;
        this.clusterY = clusterY;
        this.clusterZ = clusterZ;
    }

    private HPAGraphNode[] addEntranceNode(HPAEntrance entrance) {
        assert entrance.minX == entrance.maxX || entrance.minZ == entrance.maxZ;
        if (entrance.maxX - entrance.minX > 6) {
            return new HPAGraphNode[] { getOrAddNode(entrance.minX, entrance.minZ),
                    getOrAddNode(entrance.maxX, entrance.minZ) };
        } else if (entrance.maxZ - entrance.minZ > 6) {
            return new HPAGraphNode[] { getOrAddNode(entrance.minX, entrance.minZ),
                    getOrAddNode(entrance.minX, entrance.maxZ) };
        }
        int x = (int) (entrance.minX == entrance.maxX ? entrance.minX
                : Math.floor((entrance.minX + entrance.maxX) / 2.0));
        int z = (int) (entrance.minZ == entrance.maxZ ? entrance.minZ
                : Math.floor((entrance.minZ + entrance.maxZ) / 2.0));
        return new HPAGraphNode[] { getOrAddNode(x, z) };
    }

    public void buildFrom(List<HPACluster> clusters) {
        for (HPACluster other : clusters) {
            for (HPAGraphNode node : other.nodes) {
                if (node.x == clusterX || node.z == clusterZ || node.x == clusterX + clusterSize - 1
                        || node.z == clusterZ + clusterSize - 1) { // border node
                    nodes.add(node);
                    for (HPAGraphEdge edge : node.getEdges(level - 1)) {
                        if (edge.type == HPAGraphEdge.EdgeType.INTER) {
                            edge.from.connect(level, edge.to, edge.type, edge.weight);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            HPAGraphNode node = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                HPAGraphNode n2 = nodes.get(j);
                float weight = graph.pathfind(node, n2, level - 1).cost;
                if (!Float.isFinite(weight))
                    continue;
                node.connect(level, n2, HPAGraphEdge.EdgeType.INTRA, weight);
            }
        }
    }

    public void connect(HPACluster other, Direction direction) {
        HPAEntrance entrance = null;
        switch (direction) {
            case EAST:
                for (int z = 0; z < clusterSize; z++) {
                    if (offsetWalkable(clusterSize - 1, z) && other.offsetWalkable(0, z)) {
                        if (entrance == null) {
                            entrance = new HPAEntrance();
                            entrance.minX = entrance.maxX = clusterSize - 1;
                            entrance.minZ = z;
                        }
                        entrance.maxZ = z;
                    } else if (entrance != null) {
                        connectEntrance(other, entrance, e -> e.minX = e.maxX = 0);
                        entrance = null;
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, e -> e.minX = e.maxX = 0);
                }
                break;
            case WEST:
                for (int z = 0; z < clusterSize; z++) {
                    if (offsetWalkable(0, z) && other.offsetWalkable(clusterSize - 1, z)) {
                        if (entrance == null) {
                            entrance = new HPAEntrance();
                            entrance.minX = entrance.maxX = 0;
                            entrance.minZ = z;
                        }
                        entrance.maxZ = z;
                    } else if (entrance != null) {
                        connectEntrance(other, entrance, e -> e.minX = e.maxX = clusterSize - 1);
                        entrance = null;
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, e -> e.minX = e.maxX = clusterSize - 1);
                }
                break;
            case NORTH:
                for (int x = 0; x < clusterSize; x++) {
                    if (offsetWalkable(x, clusterSize - 1) && other.offsetWalkable(x, 0)) {
                        if (entrance == null) {
                            entrance = new HPAEntrance();
                            entrance.minZ = entrance.maxZ = clusterSize - 1;
                            entrance.minX = x;
                        }
                        entrance.maxX = x;
                    } else if (entrance != null) {
                        connectEntrance(other, entrance, e -> e.minZ = e.maxZ = 0);
                        entrance = null;
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, e -> e.minZ = e.maxZ = 0);
                }
                break;
            case SOUTH:
                for (int x = 0; x < clusterSize; x++) {
                    if (offsetWalkable(x, 0) && other.offsetWalkable(x, clusterSize - 1)) {
                        if (entrance == null) {
                            entrance = new HPAEntrance();
                            entrance.minZ = entrance.maxZ = 0;
                            entrance.minX = x;
                        }
                        entrance.maxX = x;
                    } else if (entrance != null) {
                        connectEntrance(other, entrance, e -> e.minZ = e.maxZ = clusterSize - 1);
                        entrance = null;
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, e -> e.minZ = e.maxZ = clusterSize - 1);
                }
                break;
        }
    }

    private void connectEntrance(HPACluster other, HPAEntrance entrance, Consumer<HPAEntrance> consumer) {
        HPAGraphNode[] from = addEntranceNode(entrance);
        consumer.accept(entrance);
        HPAGraphNode[] to = other.addEntranceNode(entrance);
        for (int i = 0; i < from.length; i++) {
            from[i].connect(level, to[i], HPAGraphEdge.EdgeType.INTER, 1F);
        }
    }

    public void connectIntra() {
        for (int i = 0; i < nodes.size(); i++) {
            HPAGraphNode n = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                HPAGraphNode n2 = nodes.get(j);
                float cost = pathfind(n, n2, false).cost;
                n.connect(level, n2, HPAGraphEdge.EdgeType.INTRA, cost);
            }
        }
    }

    public boolean contains(HPACluster other) {
        return clusterY == other.clusterY && clusterX + clusterSize > other.clusterX
                && clusterZ + clusterSize > other.clusterZ && other.clusterZ >= clusterZ && other.clusterX >= clusterX;
    }

    private HPAGraphNode getOrAddNode(int x, int z) {
        for (HPAGraphNode node : nodes) {
            if (node.x == this.clusterX + x && node.z == this.clusterZ + z) {
                return node;
            }
        }
        HPAGraphNode node = new HPAGraphNode(this.clusterX + x, clusterY, this.clusterZ + z);
        nodes.add(node);
        return node;
    }

    public boolean hasWalkableNodes() {
        for (int i = 0; i < clusterSize; i++) {
            for (int j = 0; j < clusterSize; j++) {
                if (offsetWalkable(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void insert(HPAGraphNode node) {
        nodes.add(node);
        for (HPAGraphNode other : nodes) {
            if (other == node)
                continue;
            float cost = pathfind(node, other, false).cost;
            if (Float.isFinite(cost)) {
                node.connect(level, other, HPAGraphEdge.EdgeType.INTRA, cost);
            }
        }
    }

    private boolean offsetWalkable(int x, int z) {
        return graph.walkable(clusterX + x, clusterY, clusterZ + z);
    }

    private AStarSolution pathfind(HPAGraphNode start, HPAGraphNode dest, boolean getPath) {
        ReversableAStarNode startNode = new ClusterNode(start.x, start.z);
        if (start.x == dest.x && start.y == dest.y && start.z == dest.y) {
            return new AStarSolution(getPath ? null : startNode.reconstructSolution(), 0);
        }
        Map<ReversableAStarNode, Float> open = new HashMap<ReversableAStarNode, Float>();
        Map<ReversableAStarNode, Float> closed = new HashMap<ReversableAStarNode, Float>();
        Queue<ReversableAStarNode> frontier = new PriorityQueue<ReversableAStarNode>();
        frontier.add(startNode);
        open.put(startNode, startNode.g);
        while (!frontier.isEmpty()) {
            ClusterNode node = (ClusterNode) frontier.poll();
            if (node.x == dest.x && node.z == dest.z) {
                return new AStarSolution(getPath ? null : node.reconstructSolution(), node.g);
            }
            closed.put(node, node.g);
            open.remove(node);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0)
                        continue;
                    if (node.x + dx < 0 || node.z + dz < 0 || node.x + dx >= 16 || node.z + dz >= 16) {
                        continue;
                    }
                    if (!offsetWalkable(dx, dz))
                        continue;
                    ClusterNode neighbour = new ClusterNode(node.x + dx, node.z + dz);
                    if (closed.containsKey(neighbour))
                        continue;
                    neighbour.parent = node;
                    // TODO: chebyshev?
                    neighbour.g = (float) (node.g
                            + Math.sqrt(Math.pow(node.x - neighbour.x, 2) + Math.pow(node.z - neighbour.z, 2)));
                    neighbour.h = (float) Math
                            .sqrt(Math.pow(neighbour.x - dest.x, 2) + Math.pow(neighbour.z - dest.z, 2));
                    if (open.containsKey(neighbour)) {
                        if (neighbour.g > open.get(neighbour))
                            continue;
                        // TODO: do we have to do this? frontier.remove(neighbour);
                    }
                    open.put(neighbour, neighbour.g);
                    frontier.add(neighbour);
                }
            }
        }
        return new AStarSolution(null, Float.POSITIVE_INFINITY);
    }

    public void remove(HPAGraphNode... nodes) {
        for (HPAGraphNode node : nodes) {
            List<List<HPAGraphEdge>> edges2 = node.edges;
            for (int i = 0; i < edges2.size(); i++) {
                List<HPAGraphEdge> edges = edges2.get(i);
                for (HPAGraphEdge edge : edges) {
                    edge.to.edges.get(i).remove(edge);
                }
            }
            this.nodes.remove(node);
        }
    }

    @Override
    public String toString() {
        return "C[" + level + "] (" + clusterX + "," + clusterY + "," + clusterZ + ")->(" + (clusterX + clusterSize - 1)
                + "," + clusterY + "," + (clusterZ + clusterSize - 1) + ")";
    }
}