package net.citizensnpcs.api.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * A list of root-level command aliases that will be accepted for this
     * command. For example: <code>{"npc", "npc2"}</code> would match both /npc
     * and /npc2.
     */
    String[] aliases();

    /**
     * A short description of the command that will be displayed with the
     * command usage and help. Translatable.
     */
    String desc();

    /**
     * Defines the flags available for this command. A flag is a single
     * character such as <code>-f</code> that will alter the behaviour of the
     * command. Each character in this string will be counted as a valid flag:
     * extra flags will be discarded. Accepts * as a catch all.
     */
    String flags() default "";

    /**
     * A longer description of the command and any flags it uses which will be
     * displayed in addition to {@link desc} in help commands. Translatable.
     */
    String help() default "";

    /**
     * The maximum number of arguments that the command will accept. Default is
     * <code>-1</code>, or an <b>unlimited</b> number of arguments.
     */
    int max() default -1;

    /**
     * Minimum number of arguments that are accepted by the command.
     */
    int min() default 0;

    /**
     * The argument modifiers accepted by the command. Also accepts
     * <code>'*'</code> as a catch all.
     */
    String[] modifiers() default "";

    /**
     * The permission of the command. The comamnd sender will get an error if
     * this is not met.
     */
    String permission() default "";

    /**
     * Command usage string that is displayed when an error occurs with the
     * command processing.
     */
    String usage() default "";
}