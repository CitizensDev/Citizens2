package net.citizensnpcs.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.bukkit.entity.EntityType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {

    boolean ownership() default false;

    boolean selected() default false;

    EntityType[] types() default { EntityType.UNKNOWN };

    EntityType[] excludedTypes() default { EntityType.UNKNOWN };
}