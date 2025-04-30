package net.citizensnpcs.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER })
public @interface Arg {
    String[] completions() default {};

    Class<? extends CompletionsProvider> completionsProvider()

    default CompletionsProvider.Identity.class;

    String defValue()

    default "";

    Class<? extends FlagValidator<?>> validator()

    default FlagValidator.Identity.class;

    int value();

    public static interface CompletionsProvider {
        public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc);

        public static class Identity implements CompletionsProvider {
            @Override
            public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
                return Collections.emptyList();
            }
        }

        public static class OptionalKeyedCompletions implements CompletionsProvider {
            private Collection<String> completions = Collections.emptyList();

            @SuppressWarnings("unchecked")
            public OptionalKeyedCompletions(String className) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (SpigotUtil.isRegistryKeyed(clazz)) {
                        Class<? extends Keyed> cast = (Class<? extends Keyed>) clazz;
                        completions = Bukkit.getRegistry(cast).stream().map(k -> k.getKey().getKey())
                                .collect(Collectors.toList());
                    } else {
                        Class<? extends Enum<?>> eclazz = (Class<? extends Enum<?>>) clazz;
                        if (eclazz.getEnumConstants() != null) {
                            completions = Lists.transform(Arrays.asList(eclazz.getEnumConstants()), Enum::name);
                        }
                    }
                } catch (ClassNotFoundException e) {
                }
            }

            @Override
            public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
                return completions;
            }
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

    public static class FloatArrayFlagValidator implements FlagValidator<float[]> {
        @Override
        public float[] validate(CommandContext args, CommandSender sender, NPC npc, String input)
                throws CommandException {
            List<Float> list = Splitter.on(',').splitToStream(input).map(s -> Float.parseFloat(s))
                    .collect(Collectors.toList());
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }
}
