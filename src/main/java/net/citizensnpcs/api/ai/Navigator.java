package net.citizensnpcs.api.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface Navigator {
    void cancelNavigation();

    float getSpeed();

    EntityTarget getEntityTarget();

    Location getTargetAsLocation();

    TargetType getTargetType();

    boolean isNavigating();

    void setSpeed(float speed);

    void setTarget(LivingEntity target, boolean aggressive);

    void setTarget(Location target);
}
