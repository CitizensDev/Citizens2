package net.citizensnpcs.api.ai;

import org.bukkit.entity.LivingEntity;

public interface EntityTarget {
    LivingEntity getTarget();

    boolean isAggressive();
}
