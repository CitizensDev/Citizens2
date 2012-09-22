package net.citizensnpcs.api.util.cuboid;

public class PrimitiveCuboid {
    private int hashcode = 0;
    int[] highCoords = { 0, 0, 0 };
    int[] highIndex = new int[3];
     int[] lowCoords = { 0, 0, 0 };
     int[] lowIndex = new int[3];

    public PrimitiveCuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
        lowCoords[0] = x1;
        lowCoords[1] = y1;
        lowCoords[2] = z1;

        highCoords[0] = x2;
        highCoords[1] = y2;
        highCoords[2] = z2;

        this.normalize();
    }

    public PrimitiveCuboid(int[] low, int[] high) {
        lowCoords = low.clone();
        highCoords = high.clone();
        normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PrimitiveCuboid)) {
            return false;
        }
        PrimitiveCuboid c = (PrimitiveCuboid) o;

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
        return lowCoords[0] <= x && lowCoords[1] <= y && lowCoords[2] <= z && highCoords[0] >= x
                && highCoords[1] >= y && highCoords[2] >= z;
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

    public boolean overlaps(PrimitiveCuboid cuboid) {
        for (int i = 0; i < 3; i++) {
            if (lowCoords[i] > cuboid.highCoords[i] || cuboid.lowCoords[i] > highCoords[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("x1=").append(lowCoords[0]).append(" y1=").append(lowCoords[1])
                .append(" z1=").append(lowCoords[2]).append(" x2=").append(highCoords[0]).append(" y2=")
                .append(highCoords[1]).append(" z2=").append(highCoords[2]).toString();
    }
}
