package net.citizensnpcs.nms.v1_14_R1.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("cattrait")
public class CatTrait extends Trait {
    @Persist
    private DyeColor collarColor = null;
    @Persist
    private boolean sitting = false;
    @Persist
    private Cat.Type type = Cat.Type.BLACK;

    public CatTrait() {
        super("cattrait");
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
        }
    }

    public void setCollarColor(DyeColor color) {
        this.collarColor = color;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setType(Cat.Type type) {
        this.type = type;
    }
}
