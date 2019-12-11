package net.citizensnpcs.nms.v1_15_R1.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;

@TraitName("cattrait")
public class CatTrait extends Trait {
    @Persist
    private DyeColor collarColor = null;
    @Persist
    private boolean lying = false;
    @Persist
    private boolean sitting = false;
    @Persist
    private Cat.Type type = Cat.Type.BLACK;

    public CatTrait() {
        super("cattrait");
    }

    public boolean isLyingDown() {
        return lying;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Cat) {
            Cat cat = (Cat) npc.getEntity();
            cat.setSitting(sitting);
            cat.setCatType(type);
            if (collarColor != null) {
                cat.setCollarColor(collarColor);
            }
            NMSImpl.setLyingDown(cat, lying);
        }
    }

    public void setCollarColor(DyeColor color) {
        this.collarColor = color;
    }

    public void setLyingDown(boolean lying) {
        this.lying = lying;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setType(Cat.Type type) {
        this.type = type;
    }
}
