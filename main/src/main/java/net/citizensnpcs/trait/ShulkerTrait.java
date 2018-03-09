package net.citizensnpcs.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Shulker;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("shulkertrait")
public class ShulkerTrait extends Trait {
    @Persist("color")
    private DyeColor color = DyeColor.PURPLE;
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
        if (npc.getEntity() instanceof Shulker) {
            NMS.setShulkerPeek((Shulker) npc.getEntity(), peek);
            NMS.setShulkerColor((Shulker) npc.getEntity(), color);
        }
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public void setPeek(int peek) {
        this.peek = peek;
    }
}
