package net.citizensnpcs.api.ai;

import org.bukkit.entity.LivingEntity;

public interface EntityTarget {
    /**
     * @return The {@link LivingEntity} being targeted.
     */
    LivingEntity getTarget();

    /**
     * @return Whether the entity target should be attacked once within range
     */
    boolean isAggressive();
}
