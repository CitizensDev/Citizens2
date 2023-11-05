package net.citizensnpcs.trait;

import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists {@link Wolf} metadata.
 *
 * @see Wolf
 */
@TraitName("wolfmodifiers")
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

    public DyeColor getCollarColor() {
        return collarColor;
    }

    public boolean isAngry() {
        return angry;
    }

    public boolean isSitting() {
        return sitting;
    }

    public boolean isTamed() {
        return tamed;
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
        collarColor = color;
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
            if (angry) {
                wolf.setTarget(wolf);
            }
            wolf.setTamed(tamed);
        }
    }
}
