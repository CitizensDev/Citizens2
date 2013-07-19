package net.citizensnpcs.api.command;

import java.lang.annotation.Annotation;

import net.citizensnpcs.api.command.exception.CommandException;

import org.bukkit.command.CommandSender;

public interface CommandAnnotationProcessor {
    /**
     * @return The {@link Annotation} class that this processor will accept.
     */
    Class<? extends Annotation> getAnnotationClass();

    /**
     * @param sender
     *            The command sender
     * @param context
     *            The context of the command, including arguments
     * @param instance
     *            The {@link Annotation} instance
     * @param args
     *            The method arguments
     * @throws CommandException
     *             If an exception occurs
     */
    void process(CommandSender sender, CommandContext context, Annotation instance, Object[] args)
            throws CommandException;
}
