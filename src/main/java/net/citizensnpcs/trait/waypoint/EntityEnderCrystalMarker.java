package net.citizensnpcs.trait.waypoint;

import net.minecraft.server.v1_6_R1.DamageSource;
import net.minecraft.server.v1_6_R1.EntityEnderCrystal;
import net.minecraft.server.v1_6_R1.World;

public class EntityEnderCrystalMarker extends EntityEnderCrystal {
    public EntityEnderCrystalMarker(World world) {
        super(world);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float i) {
        return false;
    }

    @Override
    public boolean K() {
        return false;
    }

    @Override
    public void l_() {
    }
}
