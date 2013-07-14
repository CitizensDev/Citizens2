package net.citizensnpcs.api.util.prtree;

import org.bukkit.util.Vector;

public class Region3D<T> implements MBR {
    private final T data;
    private final Vector min, max;

    public Region3D(Vector min, Vector max, T data) {
        this.min = min;
        this.max = max;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int axis) {
        switch (axis) {
            case 0:
                return max.getBlockX();
            case 1:
                return max.getBlockY();
            case 2:
                return max.getBlockZ();
        }
        return 0;
    }

    @Override
    public double getMin(int axis) {
        switch (axis) {
            case 0:
                return min.getBlockX();
            case 1:
                return min.getBlockY();
            case 2:
                return min.getBlockZ();
        }
        return 0;
    }

    @Override
    public <I> boolean intersects(I t, MBRConverter<I> converter) {
        converter.getMin(getDimensions(), t);
        return false;
    }

    @Override
    public boolean intersects(MBR other) {
        if (other.getMax(0) < min.getBlockX() || other.getMax(1) < min.getBlockY() || other.getMax(2) < min.getBlockZ())
            return false;
        if (other.getMin(0) > max.getBlockX() || other.getMin(1) > max.getBlockY() || other.getMin(2) > max.getBlockZ())
            return false;
        return false;
    }

    @Override
    public MBR union(MBR mbr) {
        Vector umin = new Vector(Math.min(min.getX(), mbr.getMin(0)), Math.min(min.getY(), mbr.getMin(1)), Math.min(
                min.getZ(), mbr.getMin(2)));
        Vector umax = new Vector(Math.max(max.getX(), mbr.getMax(0)), Math.max(max.getY(), mbr.getMax(1)), Math.max(
                max.getZ(), mbr.getMax(2)));
        return new Region3D<T>(umin, umax, data);
    }

    public static class Converter<C> implements MBRConverter<Region3D<C>> {
        @Override
        public int getDimensions() {
            return 3;
        }

        @Override
        public double getMax(int axis, Region3D<C> t) {
            return t.getMax(axis);
        }

        @Override
        public double getMin(int axis, Region3D<C> t) {
            return t.getMin(axis);
        }
    }
}
