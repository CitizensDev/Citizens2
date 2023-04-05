package net.citizensnpcs.api.util;

import org.bukkit.Location;

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

    public BoundingBox add(Location location) {
        return new BoundingBox(minX + location.getX(), minY + location.getY(), minZ + location.getZ(),
                maxX + location.getX(), maxY + location.getY(), maxZ + location.getZ());
    }

    @Override
    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(maxX) != Double.doubleToLongBits(other.maxX)) {
            return false;
        }
        if (Double.doubleToLongBits(maxY) != Double.doubleToLongBits(other.maxY)) {
            return false;
        }
        if (Double.doubleToLongBits(maxZ) != Double.doubleToLongBits(other.maxZ)) {
            return false;
        }
        if (Double.doubleToLongBits(minX) != Double.doubleToLongBits(other.minX)) {
            return false;
        }
        if (Double.doubleToLongBits(minY) != Double.doubleToLongBits(other.minY)) {
            return false;
        }
        if (Double.doubleToLongBits(minZ) != Double.doubleToLongBits(other.minZ)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp = Double.doubleToLongBits(maxX);
        result = prime * 1 + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public BoundingBox mul(double scale) {
        return new BoundingBox(minX * scale, minY * scale, minZ * scale, maxX * scale, maxY * scale, maxZ * scale);
    }

    public BoundingBox mul(double x, double y, double z) {
        return new BoundingBox(minX * x, minY * y, minZ * z, maxX * x, maxY * y, maxZ * z);
    }

    public org.bukkit.util.BoundingBox toBukkit() {
        return new org.bukkit.util.BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public EntityDim toDimensions() {
        return new EntityDim(Math.abs(maxX - minX) * 2, Math.abs(maxY - minY));
    }

    @Override
    public String toString() {
        return "BoundingBox [" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + "]";
    }

    public static BoundingBox convert(org.bukkit.util.BoundingBox bukkit) {
        return new BoundingBox(bukkit.getMinX(), bukkit.getMinY(), bukkit.getMinZ(), bukkit.getMaxX(), bukkit.getMaxY(),
                bukkit.getMaxZ());
    }

    public static final BoundingBox EMPTY = new BoundingBox(0, 0, 0, 0, 0, 0);
}
