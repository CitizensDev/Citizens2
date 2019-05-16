package net.citizensnpcs.api.hpastar;

public class Tile {
    public HPACluster cluster;
    private final String type;
    private final int x;
    private final int y;
    private final int z;

    public Tile(int x, int y, int z, String type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public boolean isObstacle() {
        return !"AIR".equals(type);
    }

}