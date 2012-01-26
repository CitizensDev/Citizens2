package net.citizensnpcs.command.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {

    boolean selected() default false;

    boolean ownership() default false;
}