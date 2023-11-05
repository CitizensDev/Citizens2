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

    /**
     * Set whether to disable gravity or not
     *
     * @param gravitate
     *            true = disable gravity, false = enable gravity
     */
    public void gravitate(boolean gravitate) {
        nogravity = gravitate;
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

    public void setEnabled(boolean enabled) {
        nogravity = enabled;
    }

    @Override
    public boolean toggle() {
        nogravity = !nogravity;
        applyImmediately();
        return nogravity;
    }
}