package net.citizensnpcs.bukkit;

import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.entity.LivingEntity;

public class BukkitLivingEntity extends BukkitEntity implements LivingEntity {
    public BukkitLivingEntity(org.bukkit.entity.LivingEntity entity) {
        super(entity);
    }

    @Override
    public int getHealth() {
        return getEntity().getHealth();
    }

    @Override
    public MobType getType() {
        return BukkitConverter.toMobType(getEntity().getType());
    }

    @Override
    public void setHealth(int health) {
        getEntity().setHealth(health);
    }

    private org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }
}
