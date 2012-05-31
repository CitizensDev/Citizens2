package net.citizensnpcs.spout;

import java.util.List;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Entity;

public class SpoutEntity implements Entity {
    protected final org.spout.api.entity.Entity entity;

    public SpoutEntity(org.spout.api.entity.Entity entity) {
        this.entity = entity;
    }

    @Override
    public List<Entity> getNearbyEntities(double dX, double dY, double dZ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorldVector getLocation() {
        return SpoutConverter.toWorldVector(entity.getPosition());
    }

    @Override
    public World getWorld() {
        return SpoutConverter.toWorld(entity.getWorld());
    }

    @Override
    public void remove() {
        entity.kill();
    }

    @Override
    public void setRotation(double yaw, double pitch) {
        entity.setRotation(entity.getRotation().rotate((float) pitch, entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ()));
    }
}
