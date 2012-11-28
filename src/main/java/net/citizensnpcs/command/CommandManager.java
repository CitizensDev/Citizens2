package net.citizensnpcs.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.command.exception.RequirementMissingException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CommandManager {

    /*
     * Mapping of commands (including aliases) with a description. Root commands
     * are stored under a key of null, whereas child commands are cached under
     * their respective Method. The child map has the key of the command name
     * (one for each alias) with the method.
     */
    private final Map<String, Method> commands = new HashMap<String, Method>();

    // Stores the injector used to getInstance.
    private Injector injector;

    // Used to store the instances associated with a method.
    private final Map<Method, Object> instances = new HashMap<Method, Object>();

    private final Map<Method, Requirements> requirements = new HashMap<Method, Requirements>();

    private final Set<Method> serverCommands = new HashSet<Method>();

    /**
     * 
     * Attempt to execute a command using the root {@link Command} given. A list
     * of method arguments may be used when calling the command handler method.
     * 
     * A command handler method should follow the form
     * <code>command(CommandContext args, CommandSender sender)</code> where
     * {@link CommandSender} can be replaced with {@link Player} to only accept
     * players. The method parameters must include the method args given, if
     * any.
     * 
     * @param command
     *            The command to execute
     * @param args
     *            The arguments of the command
     * @param sender
     *            The sender of the command
     * @param methodArgs
     *            The method arguments to be used when calling the command
     *            handler
     * @throws CommandException
     *             Any exceptions caused from execution of the command
     */
    public void execute(org.bukkit.command.Command command, String[] args, CommandSender sender,
            Object... methodArgs) throws CommandException {
        // must put command into split.
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = command.getName().toLowerCase();

        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
        executeMethod(null, newArgs, sender, newMethodArgs);
    }

    // Attempt to execute a command.
    private void executeMethod(Method parent, String[] args, CommandSender sender, Object[] methodArgs)
            throws CommandException {
        String cmdName = args[0];
        String modifier = args.length > 1 ? args[1] : "";

        Method method = commands.get(cmdName.toLowerCase() + " " + modifier.toLowerCase());
        if (method == null)
            method = commands.get(cmdName.toLowerCase() + " *");

        if (method == null && parent == null)
            throw new UnhandledCommandException();

        if (!serverCommands.contains(method) && methodArgs[1] instanceof ConsoleCommandSender)
            throw new ServerCommandException();

        if (!hasPermission(method, sender))
            throw new NoPermissionsException();

        Command cmd = method.getAnnotation(Command.class);
        CommandContext context = new CommandContext(sender, args);

        if (context.argsLength() < cmd.min())
            throw new CommandUsageException(Messages.COMMAND_TOO_FEW_ARGUMENTS, getUsage(args, cmd));

        if (cmd.max() != -1 && context.argsLength() > cmd.max())
            throw new CommandUsageException(Messages.COMMAND_TOO_MANY_ARGUMENTS, getUsage(args, cmd));

        if (!cmd.flags().contains("*")) {
            for (char flag : context.getFlags())
                if (cmd.flags().indexOf(String.valueOf(flag)) == -1)
                    throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, cmd));
        }

        methodArgs[0] = context;

        Requirements cmdRequirements = requirements.get(method);
        if (cmdRequirements != null) {
            processRequirements(sender, methodArgs, context, cmdRequirements);
        }

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

    /**
     * Searches for the closest modifier using Levenshtein distance to the given
     * top level command and modifier.
     * 
     * @param command
     *            The top level command
     * @param modifier
     *            The modifier to use as the base
     * @return The closest modifier, or empty
     */
    public String getClosestCommandModifier(String command, String modifier) {
        int minDist = Integer.MAX_VALUE;
        command = command.toLowerCase();
        String closest = "";
        for (String cmd : commands.keySet()) {
            String[] split = cmd.split(" ");
            if (split.length <= 1 || !split[0].equals(command))
                continue;
            int distance = StringHelper.getLevenshteinDistance(modifier, split[1]);
            if (minDist > distance) {
                minDist = distance;
                closest = split[1];
            }
        }

        return closest;
    }

    /**
     * Gets the {@link CommandInfo} for the given top level command and
     * modifier, or null if not found.
     * 
     * @param rootCommand
     *            The top level command
     * @param modifier
     *            The modifier (may be empty)
     * @return The command info for the command
     */
    public CommandInfo getCommand(String rootCommand, String modifier) {
        String joined = Joiner.on(' ').join(rootCommand, modifier);
        for (Entry<String, Method> entry : commands.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(joined))
                continue;
            Command commandAnnotation = entry.getValue().getAnnotation(Command.class);
            if (commandAnnotation == null)
                continue;
            return new CommandInfo(commandAnnotation, requirements.get(entry.getValue()));
        }
        return null;
    }

    /**
     * Gets all modified and root commands from the given root level command.
     * For example, if <code>/npc look</code> and <code>/npc jump</code> were
     * defined, calling <code>getCommands("npc")</code> would return
     * {@link CommandInfo}s for both commands.
     * 
     * @param command
     *            The root level command
     * @return The list of {@link CommandInfo}s
     */
    public List<CommandInfo> getCommands(String command) {
        List<CommandInfo> cmds = Lists.newArrayList();
        command = command.toLowerCase();
        for (Entry<String, Method> entry : commands.entrySet()) {
            if (!entry.getKey().startsWith(command))
                continue;
            Command commandAnnotation = entry.getValue().getAnnotation(Command.class);
            if (commandAnnotation == null)
                continue;
            cmds.add(new CommandInfo(commandAnnotation, requirements.get(entry.getValue())));
        }
        return cmds;
    }

    // Get the usage string for a command.
    private String getUsage(String[] args, Command cmd) {
        StringBuilder command = new StringBuilder("/");
        command.append(args[0] + " ");
        // removed arbitrary positioning of flags.
        command.append(cmd.usage());
        return command.toString();
    }

    /**
     * Checks to see whether there is a command handler for the given command at
     * the root level. This will check aliases as well.
     * 
     * @param cmd
     *            The command to check
     * @param modifier
     *            The modifier to check (may be empty)
     * @return Whether the command is handled
     */
    public boolean hasCommand(org.bukkit.command.Command cmd, String modifier) {
        return commands.containsKey(cmd.getName().toLowerCase() + " " + modifier.toLowerCase())
                || commands.containsKey(cmd.getName().toLowerCase() + " *");
    }

    // Returns whether a CommandSenders has permission.
    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission("citizens." + perm);
    }

    // Returns whether a player has access to a command.
    private boolean hasPermission(Method method, CommandSender sender) {
        Command cmd = method.getAnnotation(Command.class);
        if (cmd.permission().isEmpty() || hasPermission(sender, cmd.permission())
                || hasPermission(sender, "admin"))
            return true;

        return false;
    }

    private void processRequirements(CommandSender sender, Object[] methodArgs, CommandContext context,
            Requirements cmdRequirements) throws RequirementMissingException {
        NPC npc = (NPC) methodArgs[2];

        // Requirements
        if (cmdRequirements.selected()) {
            boolean canRedefineSelected = context.hasValueFlag("id") && sender.hasPermission("npc.select");
            String error = Messaging.tr(Messages.COMMAND_MUST_HAVE_SELECTED);
            if (canRedefineSelected) {
                npc = CitizensAPI.getNPCRegistry().getById(context.getFlagInteger("id"));
                if (npc == null)
                    error += ' ' + Messaging.tr(Messages.COMMAND_ID_NOT_FOUND, context.getFlagInteger("id"));
            }
            if (npc == null)
                throw new RequirementMissingException(error);
        }

        if (cmdRequirements.ownership() && npc != null && !sender.hasPermission("citizens.admin")
                && !npc.getTrait(Owner.class).isOwnedBy(sender))
            throw new RequirementMissingException(Messaging.tr(Messages.COMMAND_MUST_BE_OWNER));

        if (npc != null) {
            for (Class<? extends Trait> clazz : cmdRequirements.traits()) {
                if (!npc.hasTrait(clazz))
                    throw new RequirementMissingException(Messaging.tr(Messages.COMMAND_MISSING_TRAIT,
                            clazz.getSimpleName()));
            }
        }

        if (npc != null) {
            Set<EntityType> types = Sets.newEnumSet(Arrays.asList(cmdRequirements.types()), EntityType.class);
            if (types.contains(EntityType.UNKNOWN))
                types = EnumSet.allOf(EntityType.class);
            types.removeAll(Sets.newHashSet(cmdRequirements.excludedTypes()));

            EntityType type = npc.getTrait(MobType.class).getType();
            if (!types.contains(type)) {
                throw new RequirementMissingException(Messaging.tr(
                        Messages.COMMAND_REQUIREMENTS_INVALID_MOB_TYPE, type.getName()));
            }
        }
    }

    /**
     * Register a class that contains commands (methods annotated with
     * {@link Command}). If no dependency {@link Injector} is specified, then
     * only static methods of the class will be registered. Otherwise, new
     * instances the command class will be created and instance methods will be
     * called.
     * 
     * @see #setInjector(Injector)
     * @param clazz
     *            The class to scan
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

            // Cache the aliases too
            for (String alias : cmd.aliases()) {
                for (String modifier : cmd.modifiers()) {
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

            Class<?> senderClass = method.getParameterTypes()[1];
            if (senderClass == CommandSender.class)
                serverCommands.add(method);

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

    public static class CommandInfo {
        private final Command commandAnnotation;
        private final Requirements requirements;

        public CommandInfo(Command commandAnnotation, Requirements requirements) {
            this.commandAnnotation = commandAnnotation;
            this.requirements = requirements;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CommandInfo other = (CommandInfo) obj;
            if (commandAnnotation == null) {
                if (other.commandAnnotation != null) {
                    return false;
                }
            } else if (!commandAnnotation.equals(other.commandAnnotation)) {
                return false;
            }
            return true;
        }

        public Command getCommandAnnotation() {
            return commandAnnotation;
        }

        public Requirements getRequirements() {
            return requirements;
        }

        @Override
        public int hashCode() {
            return 31 + ((commandAnnotation == null) ? 0 : commandAnnotation.hashCode());
        }
    }

    // Logger for general errors.
    private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());
}