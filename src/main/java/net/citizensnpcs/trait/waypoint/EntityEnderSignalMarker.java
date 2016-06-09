package net.citizensnpcs.trait.waypoint;

import net.minecraft.server.v1_10_R1.DamageSource;
import net.minecraft.server.v1_10_R1.EntityEnderSignal;
import net.minecraft.server.v1_10_R1.World;

public class EntityEnderSignalMarker extends EntityEnderSignal {
    public EntityEnderSignalMarker(World world) {
        super(world);
    }

    public EntityEnderSignalMarker(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float i) {
        return false;
    }

    @Override
    public void i() {
    }

    @Override
    public void m() {
    }
}
