package net.citizensnpcs.nms.v1_8_R3.util;

import java.util.function.Supplier;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;

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
}
