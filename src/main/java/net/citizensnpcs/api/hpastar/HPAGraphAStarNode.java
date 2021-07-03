package net.citizensnpcs.api.hpastar;

public class HPAGraphAStarNode extends ReversableAStarNode {
    private final HPAGraphEdge edge;
    final HPAGraphNode node;

    public HPAGraphAStarNode(HPAGraphNode node, HPAGraphEdge edge) {
        this.node = node;
        this.edge = edge;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HPAGraphAStarNode other = (HPAGraphAStarNode) obj;
        return node.x == other.node.x && node.z == other.node.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 + node.x) + node.z;
    }

    @Override
    public String toString() {
        return (edge != null ? edge.from.toString() : "") + "->" + node.toString();
    }
}