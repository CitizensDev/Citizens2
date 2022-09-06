package net.citizensnpcs.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER })
public @interface Arg {
    Class<? extends FlagValidator<?>> validator() default Identity.class;

    int value();

    public static interface FlagValidator<T> {
        public T validate(CommandSender sender, NPC npc, String input) throws CommandException;
    }

    public static class Identity implements FlagValidator<String> {
        @Override
        public String validate(CommandSender sender, NPC npc, String input) throws CommandException {
            return input;
        }
    }
}
