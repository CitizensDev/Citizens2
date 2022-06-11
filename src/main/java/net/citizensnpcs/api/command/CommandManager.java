package net.citizensnpcs.api.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.command.exception.UnhandledCommandException;
import net.citizensnpcs.api.command.exception.WrappedCommandException;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;

public class CommandManager implements TabCompleter {
    private final Map<Class<? extends Annotation>, CommandAnnotationProcessor> annotationProcessors = Maps.newHashMap();

    /*
     * Mapping of commands (including aliases) with a description. Root commands
     * are stored under a key of null, whereas child commands are cached under
     * their respective Method. The child map has the key of the command name
     * (one for each alias) with the method.
     */
    private final Map<String, Method> commands = new HashMap<String, Method>();
    private Injector injector;
    private final Map<Method, Object> instances = new HashMap<Method, Object>();
    private final ListMultimap<Method, Annotation> registeredAnnotations = ArrayListMultimap.create();
    private final Set<Method> serverCommands = new HashSet<Method>();

    public CommandManager() {
        registerAnnotationProcessor(new RequirementsProcessor());
    }

    /**
     *
     * Attempt to execute a command using the root {@link Command} given. A list of method arguments may be used when
     * calling the command handler method.
     *
     * A command handler method should follow the form <code>command(CommandContext args, CommandSender sender)</code>
     * where {@link CommandSender} can be replaced with {@link Player} to only accept players. The method parameters
     * must include the method args given, if any.
     *
     * @param command
     *            The command to execute
     * @param args
     *            The arguments of the command
     * @param sender
     *            The sender of the command
     * @param methodArgs
     *            The method arguments to be used when calling the command handler
     * @throws CommandException
     *             Any exceptions caused from execution of the command
     */
    public void execute(org.bukkit.command.Command command, String[] args, CommandSender sender, Object... methodArgs)
            throws CommandException {
        // must put command into split.
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = command.getName().toLowerCase();

        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
        executeMethod(newArgs, sender, newMethodArgs);
    }

    private void executeHelp(String[] args, CommandSender sender) throws CommandException {
        if (!sender.hasPermission("citizens." + args[0] + ".help"))
            throw new NoPermissionsException();
        int page = 1;
        try {
            page = args.length == 3 ? Integer.parseInt(args[2]) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, args[0], args[2]);
            return;
        }
        sendHelp(sender, args[0], page);
    }

    // Attempt to execute a command.
    private void executeMethod(String[] args, CommandSender sender, Object[] methodArgs) throws CommandException {
        String cmdName = args[0].toLowerCase();
        String modifier = args.length > 1 ? args[1] : "";
        boolean help = modifier.toLowerCase().equals("help");

        Method method = commands.get(cmdName + " " + modifier.toLowerCase());
        if (method == null && !help) {
            method = commands.get(cmdName + " *");
        }

        if (method == null && help) {
            executeHelp(args, sender);
            return;
        }

        if (method == null)
            throw new UnhandledCommandException();

        if (!serverCommands.contains(method) && sender instanceof ConsoleCommandSender)
            throw new ServerCommandException();

        if (!hasPermission(method, sender))
            throw new NoPermissionsException();

        Command cmd = method.getAnnotation(Command.class);
        CommandContext context = new CommandContext(sender, args);

        if (cmd.requiresFlags() && !context.hasAnyFlags())
            throw new CommandUsageException("", getUsage(args, cmd));

        if (context.argsLength() < cmd.min())
            throw new CommandUsageException(CommandMessages.TOO_FEW_ARGUMENTS, getUsage(args, cmd));

        if (cmd.max() != -1 && context.argsLength() > cmd.max())
            throw new CommandUsageException(CommandMessages.TOO_MANY_ARGUMENTS, getUsage(args, cmd));

        if (!cmd.flags().contains("*")) {
            for (char flag : context.getFlags())
                if (cmd.flags().indexOf(String.valueOf(flag)) == -1)
                    throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, cmd));
        }

        methodArgs[0] = context;

        for (Annotation annotation : registeredAnnotations.get(method)) {
            CommandAnnotationProcessor processor = annotationProcessors.get(annotation.annotationType());
            processor.process(sender, context, annotation, methodArgs);
        }

        Object instance = instances.get(method);
        try {
            method.invoke(instance, methodArgs);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to execute command", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                if (e.getCause() instanceof CommandUsageException
                        && ((CommandUsageException) e.getCause()).getUsage() == null) {
                    ((CommandUsageException) e.getCause()).setUsage(getUsage(args, cmd));
                }
                throw (CommandException) e.getCause();
            }
            throw new WrappedCommandException(e.getCause());
        }
    }

    /**
     * A safe version of <code>execute</code> which catches and logs all errors that occur. Returns whether the command
     * handler should print usage or not.
     *
     * @see #execute(org.bukkit.command.Command, String[], CommandSender, Object...)
     * @return Whether further usage should be printed
     */
    public boolean executeSafe(org.bukkit.command.Command command, String[] args, CommandSender sender,
            Object... methodArgs) {
        try {
            try {
                execute(command, args, sender, methodArgs);
            } catch (ServerCommandException ex) {
                Messaging.sendTr(sender, CommandMessages.MUST_BE_INGAME);
            } catch (CommandUsageException ex) {
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    Messaging.sendError(sender, ex.getMessage());
                }
                Messaging.sendError(sender, ex.getUsage());
            } catch (UnhandledCommandException ex) {
                return false;
            } catch (WrappedCommandException ex) {
                throw ex.getCause();
            } catch (CommandException ex) {
                Messaging.sendError(sender, ex.getMessage());
            } catch (NumberFormatException ex) {
                Messaging.sendErrorTr(sender, CommandMessages.INVALID_NUMBER);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (sender instanceof Player) {
                Messaging.sendErrorTr(sender, CommandMessages.REPORT_ERROR);
                Messaging.sendError(sender, ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Searches for the closest modifier using Levenshtein distance to the given top level command and modifier.
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
            int distance = getLevenshteinDistance(modifier, split[1]);
            if (minDist > distance) {
                minDist = distance;
                closest = split[1];
            }
        }

        return closest;
    }

    /**
     * Gets the {@link CommandInfo} for the given top level command and modifier, or null if not found.
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
            if (!entry.getKey().equalsIgnoreCase(joined) || entry.getValue() == null)
                continue;
            Command commandAnnotation = entry.getValue().getAnnotation(Command.class);
            if (commandAnnotation == null)
                continue;
            return new CommandInfo(commandAnnotation);
        }
        return null;
    }

    /**
     * Gets all modified and root commands from the given root level command. For example, if <code>/npc look</code> and
     * <code>/npc jump</code> were defined, calling <code>getCommands("npc")</code> would return {@link CommandInfo}s
     * for both commands.
     *
     * @param command
     *            The root level command
     * @return The list of {@link CommandInfo}s
     */
    public List<CommandInfo> getCommands(String command) {
        List<CommandInfo> cmds = Lists.newArrayList();
        command = command.toLowerCase();
        for (Entry<String, Method> entry : commands.entrySet()) {
            if (!entry.getKey().startsWith(command) || entry.getValue() == null)
                continue;
            Command commandAnnotation = entry.getValue().getAnnotation(Command.class);
            if (commandAnnotation == null)
                continue;
            cmds.add(new CommandInfo(commandAnnotation));
        }
        return cmds;
    }

    private List<String> getLines(CommandSender sender, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<CommandInfo> processed = Sets.newHashSet();
        List<String> lines = new ArrayList<String>();
        for (CommandInfo info : getCommands(baseCommand)) {
            Command command = info.getCommandAnnotation();
            if (processed.contains(info)
                    || (!sender.hasPermission("citizens.admin") && !sender.hasPermission(command.permission())))
                continue;
            lines.add(format(command, baseCommand));
            if (command.modifiers().length > 0) {
                processed.add(info);
            }
        }
        Collections.sort(lines);
        return lines;
    }

    private String getUsage(String[] args, Command cmd) {
        return new StringBuilder("/").append(args[0] + " ").append(cmd.usage()).toString();
    }

    /**
     * Checks to see whether there is a command handler for the given command at the root level. This will check aliases
     * as well.
     *
     * @param cmd
     *            The command to check
     * @param modifier
     *            The modifier to check (may be empty)
     * @return Whether the command is handled
     */
    public boolean hasCommand(org.bukkit.command.Command cmd, String modifier) {
        String cmdName = cmd.getName().toLowerCase();
        return commands.containsKey(cmdName + " " + modifier.toLowerCase()) || commands.containsKey(cmdName + " *");
    }

    // Returns whether a CommandSender has permission.
    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm);
    }

    // Returns whether a player has access to a command.
    private boolean hasPermission(Method method, CommandSender sender) {
        Command cmd = method.getAnnotation(Command.class);
        if (cmd.permission().isEmpty() || hasPermission(sender, cmd.permission()) || hasPermission(sender, "admin"))
            return true;

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias,
            String[] args) {
        List<String> results = new ArrayList<String>();
        if (args.length <= 1) {
            String search = args.length == 1 ? args[0] : "";
            for (String base : commands.keySet()) {
                String[] parts = base.split(" ");
                String cmd = parts[0];
                if (!cmd.equalsIgnoreCase(command.getName()) || parts.length < 2)
                    continue;
                String modifier = parts[1];
                if (modifier.startsWith(search)) {
                    results.add(modifier);
                }
            }
            return results;
        }
        CommandInfo internalCommand = getCommand(command.getName().toLowerCase(), args[0]);
        if (internalCommand == null) {
            return results;
        }
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = command.getName().toLowerCase();
        CommandContext context = new CommandContext(sender, newArgs); // partial parse
        String flags = internalCommand.commandAnnotation.flags();
        for (int i = 0; i < flags.length(); i++) {
            char c = flags.charAt(i);
            if (!context.hasFlag(c)) {
                results.add("-" + c);
            }
        }
        Collection<String> valueFlags = internalCommand.valueFlags();
        for (String valueFlag : valueFlags) {
            if (!context.hasValueFlag(valueFlag)) {
                results.add("--" + valueFlag);
            }
        }
        return results;
    }

    /**
     * Register a class that contains commands (methods annotated with {@link Command}). If no dependency
     * {@link Injector} is specified, then only static methods of the class will be registered. Otherwise, new instances
     * the command class will be created and instance methods will be called.
     *
     * @see #setInjector(Injector)
     * @param clazz
     *            The class to scan
     */
    public void register(Class<?> clazz) {
        registerMethods(clazz, null);
    }

    /**
     * Registers an {@link CommandAnnotationProcessor} that can process annotations before a command is executed.
     *
     * Methods with the {@link Command} annotation will have the rest of their annotations scanned and stored if there
     * is a matching {@link CommandAnnotationProcessor}. Annotations that do not have a processor are discarded. The
     * scanning method uses annotations from the declaring class as a base before narrowing using the method's
     * annotations.
     *
     * @param processor
     *            The annotation processor
     */
    public void registerAnnotationProcessor(CommandAnnotationProcessor processor) {
        annotationProcessors.put(processor.getAnnotationClass(), processor);
    }

    /*
     * Register the methods of a class. This will automatically construct
     * instances as necessary.
     */
    private void registerMethods(Class<?> clazz, Method parent) {
        Object obj = injector != null ? injector.getInstance(clazz) : null;
        registerMethods(clazz, parent, obj);
    }

    // Register the methods of a class.
    private void registerMethods(Class<?> clazz, Method parent, Object obj) {
        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Command.class))
                continue;
            // We want to be able invoke with an instance
            if (!Modifier.isStatic(method.getModifiers())) {
                // Can't register this command if we don't have an instance
                if (obj == null)
                    continue;
                instances.put(method, obj);
            }

            Command cmd = method.getAnnotation(Command.class);
            // Cache the aliases too
            for (String alias : cmd.aliases()) {
                for (String modifier : cmd.modifiers()) {
                    commands.put(alias + " " + modifier, method);
                }
                if (!commands.containsKey(alias + " help")) {
                    commands.put(alias + " help", null);
                }
            }

            List<Annotation> annotations = Lists.newArrayList();
            for (Annotation annotation : method.getDeclaringClass().getAnnotations()) {
                Class<? extends Annotation> annotationClass = annotation.annotationType();
                if (annotationProcessors.containsKey(annotationClass)) {
                    annotations.add(annotation);
                }
            }
            for (Annotation annotation : method.getAnnotations()) {
                Class<? extends Annotation> annotationClass = annotation.annotationType();
                if (!annotationProcessors.containsKey(annotationClass))
                    continue;
                Iterator<Annotation> itr = annotations.iterator();
                while (itr.hasNext()) {
                    Annotation previous = itr.next();
                    if (previous.annotationType() == annotationClass) {
                        itr.remove();
                    }
                }
                annotations.add(annotation);
            }

            if (annotations.size() > 0) {
                registeredAnnotations.putAll(method, annotations);
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length <= 1 || parameterTypes[1] == CommandSender.class)
                serverCommands.add(method);
        }
    }

    public void registerTabCompletion(JavaPlugin plugin) {
        for (String string : commands.keySet()) {
            plugin.getCommand(string.split(" ")[0]).setTabCompleter(this);
        }
    }

    private void sendHelp(CommandSender sender, String name, int page) throws CommandException {
        if (name.equalsIgnoreCase("npc")) {
            name = "NPC";
        }
        Paginator paginator = new Paginator()
                .header(ChatColor.GREEN + capitalize(name) + " " + Messaging.tr(CommandMessages.COMMAND_HELP_HEADER))
                .console(sender instanceof ConsoleCommandSender);
        for (String line : getLines(sender, name.toLowerCase())) {
            paginator.addLine(line);
        }
        if (!paginator.sendPage(sender, page)) {
            throw new CommandException(CommandMessages.COMMAND_PAGE_MISSING, page);
        }
    }

    private void sendSpecificHelp(CommandSender sender, String rootCommand, String modifier) throws CommandException {
        CommandInfo info = getCommand(rootCommand, modifier);
        if (info == null)
            throw new CommandException(CommandMessages.COMMAND_MISSING, rootCommand + " " + modifier);
        Messaging.send(sender, format(info.getCommandAnnotation(), rootCommand));
        String help = Messaging.tryTranslate(info.getCommandAnnotation().help());
        if (help.isEmpty())
            return;
        Messaging.send(sender, ChatColor.AQUA + help);
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public static class CommandInfo {
        private final Command commandAnnotation;
        private List<String> valueFlags;

        public CommandInfo(Command commandAnnotation) {
            this.commandAnnotation = commandAnnotation;
        }

        private Collection<String> calculateValueFlags() {
            valueFlags = new ArrayList<String>();
            String[] usage = commandAnnotation.usage().replace("(", "").replace(")", "").split(" ");
            for (String part : usage) {
                if (part.startsWith("--")) {
                    valueFlags.add(part.split("\\|")[0].replace("--", ""));
                }
            }
            return valueFlags;
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

        @Override
        public int hashCode() {
            return 31 + ((commandAnnotation == null) ? 0 : commandAnnotation.hashCode());
        }

        public Collection<String> valueFlags() {
            return valueFlags == null ? calculateValueFlags() : valueFlags;
        }
    }

    private static String capitalize(Object string) {
        String capitalize = string.toString();
        return capitalize.length() == 0 ? ""
                : Character.toUpperCase(capitalize.charAt(0)) + capitalize.substring(1, capitalize.length());
    }

    private static String format(Command command, String alias) {
        return String.format(COMMAND_FORMAT, alias, (command.usage().isEmpty() ? "" : " " + command.usage()),
                Messaging.tryTranslate(command.desc()));
    }

    private static int getLevenshteinDistance(String s, String t) {
        if (s == null || t == null)
            throw new IllegalArgumentException("Strings must not be null");

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0)
            return m;
        else if (m == 0)
            return n;

        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; // placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++)
            p[i] = i;

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left
                // and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    private static final String COMMAND_FORMAT = "<7>/{{%s%s <7>- [[%s";

    // Logger for general errors.
    private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());
}
