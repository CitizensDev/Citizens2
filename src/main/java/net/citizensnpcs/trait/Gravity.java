package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.craftbukkit.entity.CraftEntity;

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
        net.minecraft.server.Entity entity = ((CraftEntity) npc.getBukkitEntity()).getHandle();
        entity.motY = Math.max(0, entity.motY);
    }

    @Override
    public boolean toggle() {
        return enabled = !enabled;
    }
}
