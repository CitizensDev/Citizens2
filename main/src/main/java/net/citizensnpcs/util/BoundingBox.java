package net.citizensnpcs.util;

public class BoundingBox {
    public final double maxX;
    public final double maxY;
    public final double maxZ;
    public final double minX;
    public final double minY;
    public final double minZ;

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
