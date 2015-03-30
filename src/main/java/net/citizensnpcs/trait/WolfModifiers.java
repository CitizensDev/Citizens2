package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

public class WolfModifiers extends Trait {
    @Persist("angry")
    private boolean angry;
    @Persist("collarColor")
    private DyeColor collarColor = DyeColor.RED;
    @Persist("sitting")
    private boolean sitting;
    @Persist("tamed")
    private boolean tamed;

    public WolfModifiers() {
        super("wolfmodifiers");
    }

    @Override
    public void onSpawn() {
        updateModifiers();
    }

    public void setAngry(boolean angry) {
        this.angry = angry;
        updateModifiers();
    }

    public void setCollarColor(DyeColor color) {
        this.collarColor = color;
        updateModifiers();
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
        updateModifiers();
    }

    public void setTamed(boolean tamed) {
        this.tamed = tamed;
        updateModifiers();
    }

    private void updateModifiers() {
        if (npc.getEntity() instanceof Wolf) {
            Wolf wolf = (Wolf) npc.getEntity();
            wolf.setCollarColor(collarColor);
            wolf.setSitting(sitting);
            wolf.setAngry(angry);
            wolf.setTamed(tamed);
        }
    }

	public DyeColor getCollarColor() {
		return collarColor;
	}
}
