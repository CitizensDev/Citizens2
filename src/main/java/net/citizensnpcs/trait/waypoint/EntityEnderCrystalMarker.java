package net.citizensnpcs.trait.waypoint;

import net.minecraft.server.v1_4_6.DamageSource;
import net.minecraft.server.v1_4_6.EntityEnderCrystal;
import net.minecraft.server.v1_4_6.World;

public class EntityEnderCrystalMarker extends EntityEnderCrystal {
    public EntityEnderCrystalMarker(World world) {
        super(world);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, int i) {
        return false;
    }

    @Override
    public void j_() {
    }

    @Override
    public boolean L() {
        return false;
    }
}
