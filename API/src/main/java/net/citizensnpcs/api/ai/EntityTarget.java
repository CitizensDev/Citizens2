package net.citizensnpcs.api.ai;

import org.bukkit.entity.Entity;

public interface EntityTarget {
    /**
     * @return The {@link Entity} being targeted.
     */
    Entity getTarget();

    /**
     * @return Whether the entity target should be attacked once within range
     */
    boolean isAggressive();
}
