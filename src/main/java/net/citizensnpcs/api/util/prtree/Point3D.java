package net.citizensnpcs.api.util.prtree;

import org.bukkit.util.Vector;

public class Point3D<T> implements MBR {
    private final Vector point;

    public Point3D(Vector point) {
        this.point = point;
    }

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int axis) {
        switch (axis) {
            case 0:
                return point.getBlockX();
            case 1:
                return point.getBlockY();
            case 2:
                return point.getBlockZ();
        }
        return 0;
    }

    @Override
    public double getMin(int axis) {
        return getMax(axis);
    }

    @Override
    public <I> boolean intersects(I t, MBRConverter<I> converter) {
        converter.getMin(getDimensions(), t);
        return false;
    }

    @Override
    public boolean intersects(MBR other) {
        if (other.getMax(0) < point.getBlockX() || other.getMax(1) < point.getBlockY()
                || other.getMax(2) < point.getBlockZ())
            return false;
        if (other.getMin(0) > point.getBlockX() || other.getMin(1) > point.getBlockY()
                || other.getMin(2) > point.getBlockZ())
            return false;
        return false;
    }

    @Override
    public MBR union(MBR mbr) {
        Vector umin = new Vector((point.getX() + mbr.getMin(0)) / 2.0, (point.getY() + mbr.getMin(1)) / 2.0,
                (point.getZ() + mbr.getMin(2) / 2.0));
        return new Point3D<T>(umin);
    }

    public static class Converter<C> implements MBRConverter<Point3D<C>> {
        @Override
        public int getDimensions() {
            return 3;
        }

        @Override
        public double getMax(int axis, Point3D<C> t) {
            return t.getMax(axis);
        }

        @Override
        public double getMin(int axis, Point3D<C> t) {
            return t.getMin(axis);
        }
    }

    public static <T> NodeFilter<Point3D<T>> alwaysAcceptNodeFilter() {
        return new NodeFilter<Point3D<T>>() {
            @Override
            public boolean accept(Point3D<T> t) {
                return true;
            }
        };
    }

    public static <T> DistanceCalculator<Point3D<T>> distanceCalculator() {
        return new DistanceCalculator<Point3D<T>>() {
            @Override
            public double distanceTo(Point3D<T> t, PointND p) {
                double x = p.getOrd(0);
                double y = p.getOrd(1);
                double z = p.getOrd(2);
                return Math.sqrt(Math.pow(x - t.point.getX(), 2) + Math.pow(y - t.point.getY(), 2)
                        + Math.pow(z - t.point.getZ(), 2));
            }
        };
    }
}
