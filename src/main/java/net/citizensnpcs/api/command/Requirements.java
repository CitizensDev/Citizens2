package net.citizensnpcs.api.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.citizensnpcs.api.trait.Trait;

import org.bukkit.entity.EntityType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {
    EntityType[] excludedTypes() default { EntityType.UNKNOWN };

    boolean livingEntity() default false;

    boolean ownership() default false;

    boolean selected() default false;

    Class<? extends Trait>[] traits() default {};

    EntityType[] types() default { EntityType.UNKNOWN };
}