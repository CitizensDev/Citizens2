package net.citizensnpcs.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.citizensnpcs.api.trait.Trait;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String[] aliases();

    String desc();

    String flags() default "";

    int max() default -1;

    int min() default 0;

    String[] modifiers() default "";

    String permission() default "";

    Class<? extends Trait>[] traits() default {};

    String usage() default "";
}