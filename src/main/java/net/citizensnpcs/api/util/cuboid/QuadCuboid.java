package net.citizensnpcs.api.util.cuboid;

public class QuadCuboid {
    private int hashcode = 0;
    int[] highCoords = { 0, 0, 0 };
    int[] highIndex = new int[3];
    int[] lowCoords = { 0, 0, 0 };
    int[] lowIndex = new int[3];

    public QuadCuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
        lowCoords[0] = x1;
        lowCoords[1] = y1;
        lowCoords[2] = z1;

        highCoords[0] = x2;
        highCoords[1] = y2;
        highCoords[2] = z2;

        this.normalize();
    }

    public QuadCuboid(int[] low, int[] high) {
        lowCoords = low.clone();
        highCoords = high.clone();
        normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof QuadCuboid)) {
            return false;
        }
        QuadCuboid c = (QuadCuboid) o;

        for (int i = 0; i < 3; i++) {
            if (lowCoords[i] != c.lowCoords[i]) {
                return false;
            }
            if (highCoords[i] != c.highCoords[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public boolean includesPoint(int x, int y, int z) {
        return lowCoords[0] <= x && lowCoords[1] <= y && lowCoords[2] <= z && highCoords[0] >= x && highCoords[1] >= y
                && highCoords[2] >= z;
    }

    public boolean includesPoint(int[] point) {
        return this.includesPoint(point[0], point[1], point[2]);
    }

    /**
     * Normalize the corners so that all A is <= B This is CRITICAL to have for
     * comparison to a point
     */
    private void normalize() {
        for (int i = 0; i < 3; i++) {
            if (lowCoords[i] > highCoords[i]) {
                int temp = lowCoords[i];
                lowCoords[i] = highCoords[i];
                highCoords[i] = temp;
            }
            hashcode ^= highCoords[i] ^ (~lowCoords[i]);
        }
    }

    public boolean overlaps(QuadCuboid cuboid) {
        for (int i = 0; i < 3; i++) {
            if (lowCoords[i] > cuboid.highCoords[i] || cuboid.lowCoords[i] > highCoords[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "x1=" + lowCoords[0] + " y1=" + lowCoords[1] + " z1=" + lowCoords[2] + " x2=" + highCoords[0] + " y2="
                + highCoords[1] + " z2=" + highCoords[2];
    }
}
