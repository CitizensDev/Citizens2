package net.citizensnpcs.trait;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Enable/disable gravity. Without gravity the y velocity of the NPC is always set to <code>0</code>
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
        if (!enabled || npc.getNavigator().isNavigating())
            return;
        Vector vector = npc.getEntity().getVelocity();
        vector.setY(Math.max(0, vector.getY()));
        npc.getEntity().setVelocity(vector);
    }

    @Override
    public boolean toggle() {
        return enabled = !enabled;
    }
}