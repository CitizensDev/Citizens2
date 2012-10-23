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

    @Override
    public void run() {
        if (!npc.isSpawned() || !enabled)
            return;
        Vector velocity = npc.getBukkitEntity().getVelocity();
        velocity.setY(Math.max(velocity.getY(), 0));
        npc.getBukkitEntity().setVelocity(velocity);
    }

    @Override
    public boolean toggle() {
        return enabled = !enabled;
    }
}
