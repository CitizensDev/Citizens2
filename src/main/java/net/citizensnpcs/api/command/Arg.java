package net.citizensnpcs.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER })
public @interface Arg {
    String[] completions() default {};

    Class<? extends CompletionsProvider> completionsProvider() default CompletionsProvider.Identity.class;

    String defValue() default "";

    Class<? extends FlagValidator<?>> validator() default FlagValidator.Identity.class;

    int value();

    public static interface CompletionsProvider {
        public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc);

        public static class Identity implements CompletionsProvider {
            @Override
            public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
                return Collections.emptyList();
            }
        }

        public static abstract class OptionalEnumCompletions implements CompletionsProvider {
            private Collection<String> completions = Collections.emptyList();

            @SuppressWarnings("unchecked")
            public OptionalEnumCompletions() {
                try {
                    Class<? extends Enum<?>> clazz = (Class<? extends Enum<?>>) Class.forName(getEnumClassName());
                    if (clazz.getEnumConstants() != null) {
                        completions = Lists.transform(Arrays.asList(clazz.getEnumConstants()), Enum::name);
                    }
                } catch (ClassNotFoundException e) {
                }
            }

            @Override
            public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
                return completions;
            }

            public abstract String getEnumClassName();
        }
    }

    public static interface FlagValidator<T> {
        public T validate(CommandContext args, CommandSender sender, NPC npc, String input) throws CommandException;

        public static class Identity implements FlagValidator<String> {
            @Override
            public String validate(CommandContext args, CommandSender sender, NPC npc, String input)
                    throws CommandException {
                return input;
            }
        }
    }
}
