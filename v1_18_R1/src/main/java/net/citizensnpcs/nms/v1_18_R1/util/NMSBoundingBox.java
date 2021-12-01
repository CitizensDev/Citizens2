package net.citizensnpcs.nms.v1_18_R1.util;

import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.world.phys.AABB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static BoundingBox wrap(AABB bb) {
        double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        minX = bb.minX;
        minY = bb.minY;
        minZ = bb.minZ;
        maxX = bb.maxX;
        maxY = bb.maxY;
        maxZ = bb.maxZ;
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
