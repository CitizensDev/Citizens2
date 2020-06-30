package net.citizensnpcs.api.util;

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

    public BoundingBox add(int x, int y, int z) {
        return new BoundingBox(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    public static BoundingBox convert(org.bukkit.util.BoundingBox bukkit) {
        return new BoundingBox(bukkit.getMinX(), bukkit.getMinY(), bukkit.getMinZ(), bukkit.getMaxX(), bukkit.getMaxY(),
                bukkit.getMaxZ());
    }
}
