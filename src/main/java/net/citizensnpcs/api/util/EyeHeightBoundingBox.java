package net.citizensnpcs.api.util;

import org.joml.Vector3f;

public class EyeHeightBoundingBox extends BoundingBox {
    public double eyeHeight;

    public EyeHeightBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            double eyeHeight) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.eyeHeight = eyeHeight;
    }

    public Vector3f getCenter() {
        double cx = (minX + maxX) / 2, cy = (minY + maxY) / 2, cz = (minZ + maxZ) / 2;
        return new Vector3f((float) -cx, (float) cy, (float) -cz);
    }
}
