package net.citizensnpcs.nms.v1_19_R2.util;

import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.world.phys.AABB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static AABB convert(BoundingBox box) {
        return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static BoundingBox wrap(AABB bb) {
        return new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}
