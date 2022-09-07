package net.citizensnpcs.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.citizensnpcs.api.command.Arg.CompletionsProvider;
import net.citizensnpcs.api.command.Arg.FlagValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Flag {
    String[] completions() default {};

    Class<? extends CompletionsProvider> completionsProvider() default CompletionsProvider.Identity.class;

    String defValue() default "";

    Class<? extends FlagValidator<?>> validator() default FlagValidator.Identity.class;

    String[] value();
}
