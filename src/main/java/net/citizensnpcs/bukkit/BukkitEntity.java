package net.citizensnpcs.bukkit;

import java.util.List;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Entity;

import org.bukkit.Location;

import com.google.common.collect.Lists;

public class BukkitEntity implements Entity {
    protected final org.bukkit.entity.Entity entity;

    public BukkitEntity(org.bukkit.entity.Entity entity) {
        this.entity = entity;
    }

    @Override
    public List<Entity> getNearbyEntities(double dX, double dY, double dZ) {
        List<org.bukkit.entity.Entity> bEntities = entity.getNearbyEntities(dX, dY, dZ);
        List<Entity> converted = Lists.newArrayList();
        for (org.bukkit.entity.Entity bEntity : bEntities) {
            converted.add(BukkitConverter.toEntity(bEntity));
        }
        return converted;
    }

    @Override
    public WorldVector getLocation() {
        return BukkitConverter.toWorldVector(entity.getLocation());
    }

    @Override
    public World getWorld() {
        return BukkitConverter.toWorld(entity.getWorld());
    }

    @Override
    public void remove() {
        entity.remove();
    }

    @Override
    public void setRotation(double yaw, double pitch) {
        Location loc = entity.getLocation();
        loc.setYaw((float) yaw);
        loc.setPitch((float) pitch);
        entity.teleport(loc);
    }

    @Override
    public int hashCode() {
        return 31 + ((entity == null) ? 0 : entity.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BukkitEntity other = (BukkitEntity) obj;
        if (entity == null) {
            if (other.entity != null) {
                return false;
            }
        } else if (!entity.equals(other.entity)) {
            return false;
        }
        return true;
    }
}
