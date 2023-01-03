package net.citizensnpcs.nms.v1_17_R1.util;

import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.world.phys.AABB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static BoundingBox wrap(AABB bb) {
        return new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}
