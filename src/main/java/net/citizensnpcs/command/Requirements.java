package net.citizensnpcs.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.citizensnpcs.api.abstraction.MobType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {

    boolean ownership() default false;

    boolean selected() default false;

    MobType[] types() default { MobType.UNKNOWN };

    MobType[] excludedTypes() default { MobType.UNKNOWN };
}