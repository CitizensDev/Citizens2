package net.citizensnpcs.nms.v1_20_R3.util;

import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.minecraft.world.phys.AABB;

public class NMSBoundingBox {
    private NMSBoundingBox() {
    }

    public static AABB convert(BoundingBox box) {
        return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static AABB makeBB(NPC npc, AABB def) {
        return npc == null || !npc.data().has(NPC.Metadata.BOUNDING_BOX_FUNCTION) ? def
                : NMSBoundingBox
                        .convert(npc.data().<Supplier<BoundingBox>> get(NPC.Metadata.BOUNDING_BOX_FUNCTION).get());
    }

    public static BoundingBox wrap(AABB bb) {
        return new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}
