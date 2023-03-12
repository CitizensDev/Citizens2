package net.citizensnpcs.util;

import org.bukkit.util.EulerAngle;

public class Quaternion {
    public final double w;
    public final double x;
    public final double y;
    public final double z;

    public Quaternion(double x, double y, double z, double w) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double dot(Quaternion b) {
        return x * b.x + y * b.y + z * b.z + w * b.w;
    }

    public double length() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public Quaternion mul(double m) {
        return new Quaternion(x * m, y * m, z * m, w * m);
    }

    public Quaternion norm() {
        double length = length();
        if (length > 0.0001) {
            double i = fastisqrt(length);
            return new Quaternion(i * x, i * y, i * z, i * w);
        }
        return ZERO;
    }

    private static double fastisqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - xhalf * x * x);
        return x;
    }

    public static Quaternion from(EulerAngle from) {
        return fromEuler(from.getX(), from.getY(), from.getZ());
    }

    public static Quaternion fromEuler(double x, double y, double z) {
        double c1 = Math.cos(x * 0.5);
        double c2 = Math.cos(y * 0.5);
        double c3 = Math.cos(z * 0.5);
        double s1 = Math.sin(x * 0.5);
        double s2 = Math.sin(y * 0.5);
        double s3 = Math.sin(z * 0.5);
        return new Quaternion(s1 * s2 * c3 + c1 * c2 * s3, s1 * c2 * c3 + c1 * s2 * s3, c1 * s2 * c3 - s1 * c2 * s3,
                c1 * c2 * c3 - s1 * s2 * s3);
    }

    public static Quaternion lerp(Quaternion a, Quaternion b, double t) {
        if (a.dot(b) < 0) {
            b = b.mul(-1);
        }
        return new Quaternion(a.x - t * (a.x - b.x), a.y - t * (a.y - b.y), a.z - t * (a.z - b.z),
                a.w - t * (a.w - b.w));
    }

    private static final Quaternion ZERO = new Quaternion(0, 0, 0, 0);
}
