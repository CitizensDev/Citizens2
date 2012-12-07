package net.citizensnpcs.command;

import java.lang.annotation.Annotation;

import org.bukkit.command.CommandSender;

public interface CommandAnnotationProcessor<T extends Annotation> {
    void process(CommandSender sender, CommandContext context, T instance, Object[] args);
}
