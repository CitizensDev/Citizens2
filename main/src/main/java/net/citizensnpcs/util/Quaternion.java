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

    public EulerAngle toEuler() {
        double w2 = w * w;
        double x2 = x * x;
        double y2 = y * y;
        double z2 = z * z;
        double length = x2 + y2 + z2 + w2;
        double test = x * y + z * w;
        if (test > 0.499 * length) {
            return new EulerAngle(0, 2 * Math.atan2(x, w), Math.PI / 2);
        } else if (test < -0.499 * length) {
            return new EulerAngle(0, -2 * Math.atan2(x, w), -(Math.PI / 2));
        } else {
            return new EulerAngle(Math.atan2(2 * x * w - 2 * y * z, -x2 + y2 - z2 + w2),
                    Math.atan2(2 * y * 2 - 2 * x * z, x2 - y2 - z2 + w2), Math.asin(2 * test / length));
        }
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

        double c1c2 = c1 * c2;
        double s1s2 = s1 * s2;
        return new Quaternion(s1s2 * c3 + c1c2 * s3, s1 * c2 * c3 + c1 * s2 * s3, c1 * s2 * c3 - s1 * c2 * s3,
                c1c2 * c3 - s1s2 * s3);
    }

    public static Quaternion fromEuler(EulerAngle angle) {
        return fromEuler(angle.getX(), angle.getY(), angle.getZ());
    }

    public static Quaternion nlerp(Quaternion a, Quaternion b, double t) {
        if (a.dot(b) < 0) {
            b = b.mul(-1);
        }
        return new Quaternion(a.x - t * (a.x - b.x), a.y - t * (a.y - b.y), a.z - t * (a.z - b.z),
                a.w - t * (a.w - b.w)).norm();
    }

    public static Quaternion slerp(Quaternion a, Quaternion b, double t) {
        double len = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        if (len < 0) {
            b = b.mul(-1);
            len = -len;
        }
        double sa = 1 - t;
        double sb = t;
        if (0.1 < 1 - len) {
            double theta = Math.acos(len);
            double itheta = 1 / Math.sin(theta);
            sa = Math.sin(theta * (1 - t)) * itheta;
            sb = Math.sin(theta * t) * itheta;
        }
        return new Quaternion(sa * a.x + sb * b.x, sa * a.y + sb * b.y, sa * a.z + sb * b.z, sa * a.w + sb * b.w);
    }

    private static final Quaternion ZERO = new Quaternion(0, 0, 0, 0);
}
