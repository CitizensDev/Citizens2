// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.citizensnpcs.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.builtin.Owner;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.command.exception.RequirementMissingException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandManager {

    /*
     * Mapping of commands (including aliases) with a description. Root commands
     * are stored under a key of null, whereas child commands are cached under
     * their respective Method. The child map has the key of the command name
     * (one for each alias) with the method.
     */
    private final Map<String, Method> commands = new HashMap<String, Method>();

    private final Map<String, List<Command>> subCommands = new HashMap<String, List<Command>>();

    // Stores the injector used to getInstance.
    private Injector injector;

    // Used to store the instances associated with a method.
    private final Map<Method, Object> instances = new HashMap<Method, Object>();

    private final Map<Method, Requirements> requirements = new HashMap<Method, Requirements>();

    private final Map<Method, ServerCommand> serverCommands = new HashMap<Method, ServerCommand>();

    /*
     * Attempt to execute a command. This version takes a separate command name
     * (for the root command) and then a list of following arguments.
     */
    public void execute(String cmd, String[] args, Player player, Object... methodArgs) throws CommandException {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = cmd;
        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);

        executeMethod(null, newArgs, player, newMethodArgs);
    }

    // Attempt to execute a command.
    public void execute(String[] args, Player player, Object... methodArgs) throws CommandException {
        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
        executeMethod(null, args, player, newMethodArgs);
    }

    // Attempt to execute a command.
    public void executeMethod(Method parent, String[] args, Player player, Object[] methodArgs) throws CommandException {
        String cmdName = args[0];
        String modifier = args.length > 1 ? args[1] : "";

        Method method = commands.get(cmdName.toLowerCase() + " " + modifier.toLowerCase());
        if (method == null)
            method = commands.get(cmdName.toLowerCase() + " *");

        if (method != null && methodArgs != null && serverCommands.get(method) == null
                && methodArgs[1] instanceof ConsoleCommandSender)
            throw new ServerCommandException();

        if (method == null && parent == null)
            throw new UnhandledCommandException();

        if (methodArgs[1] instanceof Player && !hasPermission(method, player))
            throw new NoPermissionsException();

        if (methodArgs[1] instanceof Player) {
            Requirements cmdRequirements = requirements.get(method);
            if (cmdRequirements != null) {
                NPC npc = (NPC) methodArgs[2];

                if (cmdRequirements.selected() && npc == null)
                    throw new RequirementMissingException("You must have an NPC selected to execute that command.");
                if (cmdRequirements.ownership() && npc != null
                        && !npc.getTrait(Owner.class).getOwner().equals(player.getName())
                        && !player.hasPermission("citizens.admin"))
                    throw new RequirementMissingException("You must be the owner of this NPC to execute that command.");
            }
        }

        Command cmd = method.getAnnotation(Command.class);

        CommandContext context = new CommandContext(args);

        if (context.argsLength() < cmd.min())
            throw new CommandUsageException("Too few arguments.", getUsage(args, cmd));

        if (cmd.max() != -1 && context.argsLength() > cmd.max())
            throw new CommandUsageException("Too many arguments.", getUsage(args, cmd));

        for (char flag : context.getFlags())
            if (cmd.flags().indexOf(String.valueOf(flag)) == -1)
                throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, cmd));

        methodArgs[0] = context;
        Object instance = instances.get(method);
        try {
            method.invoke(instance, methodArgs);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException)
                throw (CommandException) e.getCause();

            throw new WrappedCommandException(e.getCause());
        }
    }

    public String[] getAllCommandModifiers(String command) {
        Set<String> cmds = new HashSet<String>();
        for (String cmd : commands.keySet()) {
            String[] split = cmd.split(" ");
            if (split[0].equalsIgnoreCase(command) && split.length > 1)
                cmds.add(split[1]);
        }

        return cmds.toArray(new String[cmds.size()]);
    }

    // Get the usage string for a command.
    private String getUsage(String[] args, Command cmd) {
        StringBuilder command = new StringBuilder();

        command.append("/");

        command.append(args[0] + " ");

        // removed arbitrary positioning of flags.
        command.append(cmd.usage());

        return command.toString();
    }

    /*
     * Checks to see whether there is a command named such at the root level.
     * This will check aliases as well.
     */
    public boolean hasCommand(String command, String modifier) {
        return commands.containsKey(command.toLowerCase() + " " + modifier.toLowerCase())
                || commands.containsKey(command.toLowerCase() + " *");
    }

    public List<Command> getCommands(String command) {
        if (subCommands.containsKey(command))
            return subCommands.get(command);
        List<Command> cmds = new ArrayList<Command>();
        for (Entry<String, Method> entry : commands.entrySet()) {
            if (!entry.getKey().split(" ")[0].equalsIgnoreCase(command)
                    || !entry.getValue().isAnnotationPresent(Command.class))
                continue;
            cmds.add(entry.getValue().getAnnotation(Command.class));
        }
        return cmds;
    }

    // Returns whether a player has access to a command.
    private boolean hasPermission(Method method, Player player) {
        Command cmd = method.getAnnotation(Command.class);
        if (cmd.permission().isEmpty() || hasPermission(player, cmd.permission())
                || hasPermission(player, "citizens.admin"))
            return true;

        return false;
    }

    // Returns whether a player has permission.
    private boolean hasPermission(Player player, String perm) {
        return player.hasPermission("citizens." + perm);
    }

    /*
     * Register an class that contains commands (denoted by Command. If no
     * dependency injector is specified, then the methods of the class will be
     * registered to be called statically. Otherwise, new instances will be
     * created of the command classes and methods will not be called statically.
     */
    public void register(Class<?> clazz) {
        registerMethods(clazz, null);
    }

    /*
     * Register the methods of a class. This will automatically construct
     * instances as necessary.
     */
    private void registerMethods(Class<?> clazz, Method parent) {
        Object obj = injector.getInstance(clazz);
        registerMethods(clazz, parent, obj);
    }

    // Register the methods of a class.
    private void registerMethods(Class<?> clazz, Method parent, Object obj) {
        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Command.class))
                continue;
            boolean isStatic = Modifier.isStatic(method.getModifiers());

            Command cmd = method.getAnnotation(Command.class);
            String[] modifiers = cmd.modifiers();

            // Cache the aliases too
            for (String alias : cmd.aliases()) {
                for (String modifier : modifiers) {
                    commands.put(alias + " " + modifier, method);
                }
            }

            Requirements cmdRequirements = null;
            if (method.getDeclaringClass().isAnnotationPresent(Requirements.class))
                cmdRequirements = method.getDeclaringClass().getAnnotation(Requirements.class);

            if (method.isAnnotationPresent(Requirements.class))
                cmdRequirements = method.getAnnotation(Requirements.class);

            if (requirements != null)
                requirements.put(method, cmdRequirements);

            ServerCommand serverCommand = null;
            if (method.isAnnotationPresent(ServerCommand.class))
                serverCommand = method.getAnnotation(ServerCommand.class);

            if (serverCommand != null)
                serverCommands.put(method, serverCommand);

            // We want to be able invoke with an instance
            if (!isStatic) {
                // Can't register this command if we don't have an instance
                if (obj == null)
                    continue;

                instances.put(method, obj);
            }
        }
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    // Logger for general errors.
    private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());
}