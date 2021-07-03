package net.citizensnpcs.api.hpastar;

public class ClusterNode extends ReversableAStarNode {
    int x, z;

    public ClusterNode(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClusterNode other = (ClusterNode) obj;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 + x) + z;
    }
}