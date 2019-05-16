package net.citizensnpcs.api.hpastar;

public class HPAGraphAStarNode extends SimpleAStarNode {
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
        final int prime = 31;
        int result = prime + node.x;
        return prime * result + node.z;
    }

    @Override
    public String toString() {
        return (edge != null ? edge.from.toString() : "") + "->" + node.toString();
    }
}