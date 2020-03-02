package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Enable/disable Minecraft's gravity.
 */
@TraitName("gravity")
public class Gravity extends Trait implements Toggleable {
    @Persist
    private boolean enabled;

    public Gravity() {
        super("gravity");
    }

    /**
     * Set whether to disable gravity or not
     *
     * @param gravitate
     *            true = disable gravity, false = enable gravity
     */
    public void gravitate(boolean gravitate) {
        enabled = gravitate;
    }

    public boolean hasGravity() {
        return !enabled;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        NMS.setNoGravity(npc.getEntity(), enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean toggle() {
        return enabled = !enabled;
    }
}