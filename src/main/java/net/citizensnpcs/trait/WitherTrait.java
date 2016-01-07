package net.citizensnpcs.trait;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.Wither;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.minecraft.server.v1_8_R3.EntityWither;

@TraitName("withertrait")
public class WitherTrait extends Trait {
    @Persist("charged")
    private boolean charged = false;

    public WitherTrait() {
        super("withertrait");
    }

    public boolean isCharged() {
        return charged;
    }

    @Override
    public void onSpawn() {
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Wither) {
            Wither wither = (Wither) npc.getEntity();
            EntityWither handle = ((CraftWither) wither).getHandle();
            handle.r(charged ? 20 : 0);
        }
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }
}
