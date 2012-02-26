package net.citizensnpcs.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {

    boolean ownership() default false;

    boolean selected() default false;
}