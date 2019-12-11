package net.citizensnpcs.nms.v1_15_R1.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Shulker;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.citizensnpcs.util.NMS;

@TraitName("shulkertrait")
public class ShulkerTrait extends Trait {
    @Persist("color")
    private DyeColor color = DyeColor.PURPLE;
    private int lastPeekSet = 0;
    @Persist("peek")
    private int peek = 0;

    public ShulkerTrait() {
        super("shulkertrait");
    }

    @Override
    public void onSpawn() {
        setPeek(peek);
    }

    @Override
    public void run() {
        if (color == null) {
            color = DyeColor.PURPLE;
        }
        if (npc.getEntity() instanceof Shulker) {
            if (peek != lastPeekSet) {
                NMS.setPeekShulker((Shulker) npc.getEntity(), peek);
                lastPeekSet = peek;
            }
            NMSImpl.setShulkerColor((Shulker) npc.getEntity(), color);
        }
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public void setPeek(int peek) {
        this.peek = peek;
        lastPeekSet = -1;
    }
}
