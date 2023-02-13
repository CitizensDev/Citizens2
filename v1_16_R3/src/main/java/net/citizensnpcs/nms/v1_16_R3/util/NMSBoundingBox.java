package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static AxisAlignedBB convert(BoundingBox box) {
        return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static AxisAlignedBB makeBB(NPC npc, AxisAlignedBB def) {
        return npc == null || !npc.data().has(NPC.Metadata.BOUNDING_BOX_FUNCTION) ? def
                : convert(npc.data().<Supplier<BoundingBox>> get(NPC.Metadata.BOUNDING_BOX_FUNCTION).get());
    }

    public static BoundingBox wrap(AxisAlignedBB bb) {
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
