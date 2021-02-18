package net.citizensnpcs.trait;

import org.bukkit.entity.EnderCrystal;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists EnderCrystal metadata.
 *
 * @see EnderCrystal
 */
@TraitName("endercrystaltrait")
public class EnderCrystalTrait extends Trait {
    @Persist
    private boolean showBase;

    public EnderCrystalTrait() {
        super("endercrystaltrait");
    }

    public boolean isShowBase() {
        return showBase;
    }

    @Override
    public void onSpawn() {
        updateModifiers();
    }

    public void setShowBase(boolean showBase) {
        this.showBase = showBase;
        updateModifiers();
    }

    private void updateModifiers() {
        if (!(npc.getEntity() instanceof EnderCrystal) || !SUPPORT_SHOW_BOTTOM)
            return;
        EnderCrystal crystal = (EnderCrystal) npc.getEntity();
        try {
            crystal.setShowingBottom(showBase);
        } catch (NoSuchMethodError err) {
            SUPPORT_SHOW_BOTTOM = false;
        }
    }

    private static boolean SUPPORT_SHOW_BOTTOM = true;
}
