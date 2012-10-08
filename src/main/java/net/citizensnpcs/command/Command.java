package net.citizensnpcs.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String[] aliases();

    String desc();

    String flags() default "";

    String help() default "";

    int max() default -1;

    int min() default 0;

    String[] modifiers() default "";

    String permission() default "";

    String usage() default "";
}