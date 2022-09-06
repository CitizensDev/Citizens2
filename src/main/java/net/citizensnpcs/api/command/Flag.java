package net.citizensnpcs.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.citizensnpcs.api.command.Arg.FlagValidator;
import net.citizensnpcs.api.command.Arg.Identity;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Flag {
    String[] completions() default {};

    String defValue() default "";

    Class<? extends FlagValidator<?>> validator() default Identity.class;

    String[] value();
}
