package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.util.Vector;

public class Gravity extends Trait implements Toggleable {
    @Persist
    private boolean enabled;

    public Gravity() {
        super("gravity");
    }

    public void gravitate(boolean gravitate) {
        enabled = gravitate;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !enabled)
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
