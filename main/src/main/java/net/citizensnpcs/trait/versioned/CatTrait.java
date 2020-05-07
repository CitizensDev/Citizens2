package net.citizensnpcs.trait.versioned;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Ocelot.Type;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

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
        if (!(npc.getEntity() instanceof Cat))
            return;
        Cat cat = (Cat) npc.getEntity();
        cat.setSitting(sitting);
        cat.setCatType(type);
        if (collarColor != null) {
            cat.setCollarColor(collarColor);
        }
        NMS.setLyingDown(cat, lying);
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

    public void setType(Type type2) {
        switch (type2) {
            case WILD_OCELOT:
                this.type = Cat.Type.CALICO;
                break;
            case BLACK_CAT:
                this.type = Cat.Type.BLACK;
                break;
            case RED_CAT:
                this.type = Cat.Type.RED;
                break;
            case SIAMESE_CAT:
                this.type = Cat.Type.SIAMESE;
                break;
        }
    }
}
