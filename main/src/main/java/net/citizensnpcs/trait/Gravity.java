package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Enable/disable Minecraft's gravity.
 */
@TraitName("gravity")
public class Gravity extends Trait {
    @Persist("enabled")
    private boolean nogravity;

    public Gravity() {
        super("gravity");
    }

    private void applyImmediately() {
        if (nogravity && npc.getEntity() != null) {
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0));
            NMS.setNoGravity(npc.getEntity(), nogravity);
        }
    }

    public boolean hasGravity() {
        return !nogravity;
    }

    @Override
    public void onSpawn() {
        applyImmediately();
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        NMS.setNoGravity(npc.getEntity(), nogravity);
    }

    /**
     * Set whether to have gravity or not
     */
    public void setHasGravity(boolean hasGravity) {
        nogravity = !hasGravity;
    }

    public boolean toggle() {
        nogravity = !nogravity;
        applyImmediately();
        return nogravity;
    }
}