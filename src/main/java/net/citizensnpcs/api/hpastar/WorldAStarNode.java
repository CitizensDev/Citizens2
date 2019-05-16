package net.citizensnpcs.api.hpastar;

public class WorldAStarNode extends SimpleAStarNode {
    int x, z;

    public WorldAStarNode(int x, int z) {
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
        WorldAStarNode other = (WorldAStarNode) obj;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + x;
        return prime * result + z;
    }
}