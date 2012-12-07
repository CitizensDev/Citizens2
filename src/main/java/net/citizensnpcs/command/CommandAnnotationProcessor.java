package net.citizensnpcs.command;

import java.lang.annotation.Annotation;

import net.citizensnpcs.command.exception.CommandException;

import org.bukkit.command.CommandSender;

public interface CommandAnnotationProcessor {
    Class<? extends Annotation> getAnnotationClass();

    void process(CommandSender sender, CommandContext context, Annotation instance, Object[] args)
            throws CommandException;
}
