package net.citizensnpcs.nms.v1_13_R2.util;

import java.lang.reflect.Field;

import net.citizensnpcs.util.BoundingBox;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static BoundingBox wrap(AxisAlignedBB bb) {
        double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        try {
            minX = bb.minX;
            minY = bb.minY;
            minZ = bb.minZ;
            maxX = bb.maxX;
            maxY = bb.maxY;
            maxZ = bb.maxZ;
        } catch (NoSuchFieldError ex) {
            try {
                minX = a.getDouble(bb);
                minY = b.getDouble(bb);
                minZ = c.getDouble(bb);
                maxX = d.getDouble(bb);
                maxY = e.getDouble(bb);
                maxZ = f.getDouble(bb);
            } catch (Exception ex2) {
                ex.printStackTrace();
            }
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static final Field a = NMS.getField(AxisAlignedBB.class, "a", false);
    private static final Field b = NMS.getField(AxisAlignedBB.class, "b", false);
    private static final Field c = NMS.getField(AxisAlignedBB.class, "c", false);
    private static final Field d = NMS.getField(AxisAlignedBB.class, "d", false);
    private static final Field e = NMS.getField(AxisAlignedBB.class, "e", false);
    private static final Field f = NMS.getField(AxisAlignedBB.class, "f", false);
}
