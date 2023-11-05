package net.citizensnpcs.api.util;

import java.util.Objects;

import org.bukkit.entity.Entity;

public class EntityDim {
    public final float height;
    public final float width;

    public EntityDim(double width, double height) {
        this((float) width, (float) height);
    }

    public EntityDim(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public EntityDim clone() {
        return new EntityDim(width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        EntityDim other = (EntityDim) obj;
        if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
            return false;
        if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width);
    }

    public EntityDim mul(float scale) {
        return new EntityDim(width * scale, height * scale);
    }

    @Override
    public String toString() {
        return "EntityDim [height=" + height + ", width=" + width + "]";
    }

    public static EntityDim from(Entity entity) {
        return new EntityDim(entity.getWidth(), entity.getHeight());
    }
}
