package net.citizensnpcs.api.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.joml.Quaternionfc;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Arg.CompletionsProvider;
import net.citizensnpcs.api.command.Arg.FlagValidator;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.command.exception.UnhandledCommandException;
import net.citizensnpcs.api.command.exception.WrappedCommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;

public class CommandManager implements TabCompleter {
    private final Map<Class<? extends Annotation>, CommandAnnotationProcessor> annotationProcessors = Maps.newHashMap();
    /*
     * Mapping of commands (including aliases) with a description. Root commands
     * are stored under a key of null, whereas child commands are cached under
     * their respective Method. The child map has the key of the command name
     * (one for each alias) with the method.
     */
    private final Map<String, CommandInfo> commands = Maps.newHashMap();
    private TimeUnit defaultDurationUnits;
    private Injector injector;
    private Function<Command, String> translationPrefixProvider;

    public CommandManager() {
        registerAnnotationProcessor(new RequirementsProcessor());
    }

    /**
     *
     * Attempt to execute a command using the root {@link Command} given. A list of method arguments may be used when
     * calling the command handler method.
     * <p>
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
        newArgs[0] = command.getName().toLowerCase(Locale.ROOT);

        Object[] newMethodArgs = new Object[methodArgs.length + 1];
        System.arraycopy(methodArgs, 0, newMethodArgs, 1, methodArgs.length);
        executeCommand(newArgs, sender, newMethodArgs);
    }

    private void executeCommand(String[] args, CommandSender sender, Object[] methodArgs) throws CommandException {
        String cmdName = args[0].toLowerCase(Locale.ROOT);
        String modifier = args.length > 1 ? args[1] : "";
        boolean help = modifier.equalsIgnoreCase("help");

        CommandInfo info = getCommand(cmdName, modifier);
        if (info == null || info.method == null) {
            if (help) {
                executeHelp(args, sender);
                return;
            }
            info = commands.get(cmdName + " *");
        }
        if (info == null && args.length > 2) {
            info = getCommand(cmdName, args[1], args[2]);
        }
        if (info == null)
            throw new UnhandledCommandException();

        if (!info.serverCommand && sender instanceof ConsoleCommandSender)
            throw new ServerCommandException();

        if (!hasPermission(info, sender))
            throw new NoPermissionsException();

        Command cmd = info.commandAnnotation;
        if (cmd.parsePlaceholders()) {
            NPC npc = methodArgs.length > 2 && methodArgs[2] instanceof NPC ? (NPC) methodArgs[2] : null;
            for (int i = 1; i < args.length; i++) {
                args[i] = Placeholders.replace(args[i], sender, npc);
            }
        }
        CommandContext context = new CommandContext(sender, args);

        if (cmd.requiresFlags() && !context.hasAnyFlags())
            throw new CommandUsageException("", getUsage(args, cmd));

        if (context.argsLength() < cmd.min())
            throw new CommandUsageException(CommandMessages.TOO_FEW_ARGUMENTS, getUsage(args, cmd));

        if (cmd.max() != -1 && context.argsLength() > cmd.max())
            throw new CommandUsageException(CommandMessages.TOO_MANY_ARGUMENTS, getUsage(args, cmd));

        if (!cmd.flags().contains("*")) {
            for (char flag : context.getFlags()) {
                if (cmd.flags().indexOf(String.valueOf(flag)) == -1)
                    throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, cmd));
            }
        }
        methodArgs[0] = context;

        for (Annotation annotation : info.annotations) {
            CommandAnnotationProcessor processor = annotationProcessors.get(annotation.annotationType());
            processor.process(sender, context, annotation, methodArgs);
        }
        if (info.methodArguments.size() > 0) {
            methodArgs = Arrays.copyOf(methodArgs, methodArgs.length + info.methodArguments.size());
            for (Entry<Integer, InjectedCommandArgument> entry : info.methodArguments.entrySet()) {
                Class<?> desiredType = entry.getValue().paramType;
                Object val = entry.getValue().getInput(context);

                if (val == null) {
                } else if (entry.getValue().validator != null) {
                    val = entry.getValue().validator.validate(context, sender,
                            methodArgs.length > 2 && methodArgs[2] instanceof NPC ? (NPC) methodArgs[2] : null,
                            val.toString());
                } else if (desiredType == Player.class) {
                    try {
                        val = Bukkit.getPlayer(UUID.fromString(val.toString()));
                    } catch (IllegalArgumentException ex) {
                        val = Bukkit.getPlayerExact(val.toString());
                    }
                } else if (SpigotUtil.isRegistryKeyed(desiredType) && SpigotUtil.getKey(val.toString()) != null) {
                    val = Bukkit.getRegistry((Class<? extends Keyed>) desiredType)
                            .get(SpigotUtil.getKey(val.toString()));
                } else if (desiredType == Material.class) {
                    val = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(val.toString(), false)
                            : Material.matchMaterial(val.toString());
                } else if (Enum.class.isAssignableFrom(desiredType)) {
                    val = matchEnum((Enum[]) desiredType.getEnumConstants(), val.toString().toUpperCase(Locale.ROOT));
                } else if (desiredType == double.class || desiredType == Double.class) {
                    val = Double.parseDouble(val.toString());
                } else if (desiredType == int.class || desiredType == Integer.class) {
                    val = Integer.parseInt(val.toString());
                } else if (desiredType == boolean.class || desiredType == Boolean.class) {
                    val = Boolean.parseBoolean(val.toString());
                } else if (desiredType == float.class || desiredType == Float.class) {
                    val = Float.parseFloat(val.toString());
                } else if (desiredType == Location.class) {
                    val = CommandContext.parseLocation(context.getSenderLocation(), val.toString());
                } else if (desiredType == ItemStack.class) {
                    val = SpigotUtil.parseItemStack(null, val.toString());
                } else if (desiredType == UUID.class) {
                    val = UUID.fromString(val.toString());
                } else if (desiredType == Duration.class) {
                    val = SpigotUtil.parseDuration(val.toString(), defaultDurationUnits);
                } else if (desiredType == Vector.class) {
                    val = CommandContext.parseVector(val.toString());
                } else if (Quaternionfc.class.isAssignableFrom(desiredType)) {
                    val = CommandContext.parseQuaternion(val.toString());
                }
                methodArgs[entry.getKey()] = val;
            }
        }
        try {
            info.method.invoke(info.instance, methodArgs);
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

    /**
     * A safe version of {@link #execute(org.bukkit.command.Command, String[], CommandSender, Object...)} which catches
     * and logs all {@link Exception}s that occur.
     *
     * @see #execute(org.bukkit.command.Command, String[], CommandSender, Object...)
     * @return Whether command usage should be printed
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
                Messaging.sendErrorTr(sender, CommandMessages.UNKNOWN_COMMAND);
                return false;
            } catch (WrappedCommandException ex) {
                if (ex.getCause() instanceof NumberFormatException) {
                    if (Messaging.isDebugging()) {
                        ex.printStackTrace();
                    }
                    Messaging.sendErrorTr(sender, CommandMessages.INVALID_NUMBER);
                } else
                    throw ex.getCause();
            } catch (CommandException ex) {
                Messaging.sendError(sender, ex.getMessage());
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

    private String format(Command command, String alias) {
        String description = command.desc();
        if (translationPrefixProvider != null && description.isEmpty()) {
            description = translationPrefixProvider.apply(command) + ".description";
        }
        return String.format(COMMAND_FORMAT, alias, command.usage().isEmpty() ? "" : " " + command.usage(),
                Messaging.tryTranslate(description));
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
            if (split.length <= 1 || !split[0].equals(command)) {
                continue;
            }
            int distance = getLevenshteinDistance(modifier, split[1]);
            if (minDist > distance) {
                minDist = distance;
                closest = split[1];
            }
        }
        return closest;
    }

    /**
     * Gets the {@link CommandInfo} for the given command parts, or <code>null</code> if not found.
     *
     * @param commandParts
     *            The parts of the command
     * @return The command info for the command
     */
    public CommandInfo getCommand(String... commandParts) {
        return commands.get(Joiner.on(' ').join(commandParts).toLowerCase(Locale.ROOT));
    }

    /**
     * Gets all modified and root commands from the given root level command. For example, if <em>/npc look</em> and
     * <em>/npc jump</em> were defined, calling <code>getCommands("npc")</code> would return {@link CommandInfo}s for
     * both commands.
     *
     * @param topLevelCommand
     *            The root level command
     * @return The list of {@link CommandInfo}s
     */
    public List<CommandInfo> getCommands(String topLevelCommand) {
        topLevelCommand = topLevelCommand.toLowerCase(Locale.ROOT);
        List<CommandInfo> cmds = Lists.newArrayList();
        for (Entry<String, CommandInfo> entry : commands.entrySet()) {
            if (!entry.getKey().startsWith(topLevelCommand) || entry.getValue() == null)
                continue;

            cmds.add(entry.getValue());
        }
        return cmds;
    }

    private List<String> getLines(CommandSender sender, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<CommandInfo> processed = Sets.newHashSet();
        List<String> lines = new ArrayList<>();
        for (CommandInfo info : getCommands(baseCommand)) {
            Command command = info.getCommandAnnotation();
            if (processed.contains(info)
                    || !sender.hasPermission("citizens.admin") && !sender.hasPermission(command.permission())) {
                continue;
            }
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
    public boolean hasCommand(org.bukkit.command.Command cmd, String... modifier) {
        String cmdName = cmd.getName().toLowerCase(Locale.ROOT);
        String[] parts = new String[modifier.length + 1];
        System.arraycopy(modifier, 0, parts, 1, modifier.length);
        parts[0] = cmdName;
        return hasCommand(parts);
    }

    /**
     * Checks to see whether there is a command handler for the given command parts at the root level. This will check
     * aliases as well.
     *
     * @param parts
     *            The parts to check (must not be empty)
     * @return Whether the command is handled
     */
    public boolean hasCommand(String... parts) {
        if (parts == null || parts.length == 0)
            throw new IllegalArgumentException("parts must not be empty");
        return commands.containsKey(Joiner.on(' ').join(parts)) || commands.containsKey(parts[0] + " *");
    }

    private boolean hasPermission(CommandInfo method, CommandSender sender) {
        Command cmd = method.commandAnnotation;
        return cmd.permission().isEmpty() || sender.hasPermission(cmd.permission())
                || sender.hasPermission("citizens.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias,
            String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length <= 2 && args[0].equalsIgnoreCase("help"))
            return getCommands(command.getName().toLowerCase(Locale.ROOT)).stream()
                    .map(info -> info.commandAnnotation.modifiers().length > 0 ? info.commandAnnotation.modifiers()[0]
                            : null)
                    .collect(Collectors.toList());

        if (args.length <= 1) {
            String search = args.length == 1 ? args[0] : "";
            for (String base : commands.keySet()) {
                String[] parts = base.split(" ");
                String cmd = parts[0];
                if (!cmd.equalsIgnoreCase(command.getName()) || parts.length < 2) {
                    continue;
                }
                String modifier = parts[1];
                if (modifier.startsWith(search)) {
                    results.add(modifier);
                }
            }
            return results;
        }
        CommandInfo cmd = getCommand(command.getName(), args[0]);
        if (cmd == null && args.length > 1) {
            cmd = getCommand(command.getName(), args[0], args[1]);
        }
        if (cmd == null)
            return results;

        // partial parse
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = command.getName().toLowerCase(Locale.ROOT);
        CommandContext context = new CommandContext(false, sender, newArgs);

        results.addAll(cmd.getArgTabCompletions(context, sender, args.length - 1));

        String lastArg = (newArgs.length >= 2 ? newArgs[newArgs.length - 2] : newArgs[newArgs.length - 1])
                .toLowerCase(Locale.ROOT);
        String hyphenStrippedArg = lastArg.replaceFirst("--", "");

        if (lastArg.startsWith("--") && cmd.valueFlags().contains(hyphenStrippedArg)) {
            results.addAll(cmd.getFlagTabCompletions(context, sender, hyphenStrippedArg));
        } else {
            lastArg = newArgs[newArgs.length - 1];
            hyphenStrippedArg = lastArg.replaceFirst("--", "");
            boolean isEmpty = lastArg.isEmpty() || ImmutableSet.of("-", "--").contains(lastArg);
            for (String valueFlag : cmd.valueFlags()) {
                if (lastArg.startsWith("--") && valueFlag.startsWith(hyphenStrippedArg)
                        || isEmpty && !context.hasValueFlag(valueFlag)) {
                    results.add("--" + valueFlag);
                }
            }
            String flags = cmd.commandAnnotation.flags();
            for (int i = 0; i < flags.length(); i++) {
                char c = flags.charAt(i);
                if (lastArg.isEmpty() && !context.hasFlag(c)) {
                    results.add("-" + c);
                }
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
     * <p>
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
            if (!method.isAnnotationPresent(Command.class)
                    || !Modifier.isStatic(method.getModifiers()) && obj == null) {
                continue;
            }
            Command cmd = method.getAnnotation(Command.class);
            CommandInfo info = new CommandInfo(cmd, method);

            info.instance = obj;

            List<Annotation> annotations = Lists.newArrayList();
            for (Annotation annotation : method.getDeclaringClass().getAnnotations()) {
                Class<? extends Annotation> annotationClass = annotation.annotationType();
                if (annotationProcessors.containsKey(annotationClass)) {
                    annotations.add(annotation);
                }
            }
            for (Annotation annotation : method.getAnnotations()) {
                Class<? extends Annotation> annotationClass = annotation.annotationType();
                if (!annotationProcessors.containsKey(annotationClass)) {
                    continue;
                }
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
                info.annotations = annotations;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length <= 1 || parameterTypes[1] == CommandSender.class) {
                info.serverCommand = true;
            }
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                for (Annotation ann : parameters[i].getAnnotations()) {
                    if (ann instanceof Flag) {
                        info.addFlagAnnotation(i, parameterTypes[i], (Flag) ann);
                    } else if (ann instanceof Arg) {
                        info.addArgAnnotation(i, parameterTypes[i], (Arg) ann);
                    }
                }
            }
            for (String alias : cmd.aliases()) {
                for (String modifier : cmd.modifiers()) {
                    commands.put(alias + " " + modifier, info);
                }
                if (!commands.containsKey(alias + " help")) {
                    commands.put(alias + " help", null);
                }
            }
        }
    }

    public void registerTabCompletion(JavaPlugin plugin) {
        for (String string : commands.keySet()) {
            PluginCommand command = plugin.getCommand(string.split(" ")[0]);
            if (command == null) {
                continue;
            }
            command.setTabCompleter(this);
        }
    }

    private void sendHelp(CommandSender sender, String name, int page) throws CommandException {
        if (name.equalsIgnoreCase("npc")) {
            name = "NPC";
        }
        Paginator paginator = new Paginator()
                .header(capitalize(name) + " " + Messaging.tr(CommandMessages.COMMAND_HELP_HEADER))
                .console(sender instanceof ConsoleCommandSender);
        for (String line : getLines(sender, name.toLowerCase(Locale.ROOT))) {
            paginator.addLine(line);
        }
        if (!paginator.sendPage(sender, page))
            throw new CommandException(CommandMessages.COMMAND_PAGE_MISSING, page);
    }

    private void sendSpecificHelp(CommandSender sender, String rootCommand, String modifier) throws CommandException {
        CommandInfo info = getCommand(rootCommand, modifier);
        if (info == null)
            throw new CommandException(CommandMessages.COMMAND_MISSING, rootCommand + " " + modifier);
        Messaging.send(sender, format(info.getCommandAnnotation(), rootCommand));
        String help = Messaging.tryTranslate(info.getCommandAnnotation().help());
        if (translationPrefixProvider != null) {
            String helpKey = translationPrefixProvider.apply(info.getCommandAnnotation()) + ".help";
            String attemptedTranslation = Messaging.tryTranslate(helpKey);
            if (!helpKey.equals(attemptedTranslation) && !attemptedTranslation.isEmpty()) {
                help = attemptedTranslation;
            }
        }
        if (help.isEmpty())
            return;
        Messaging.send(sender, "<aqua>" + help);
    }

    public void setDefaultDurationUnits(TimeUnit unit) {
        this.defaultDurationUnits = unit;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void setTranslationPrefixProvider(Function<Command, String> provider) {
        this.translationPrefixProvider = provider;
    }

    public class CommandInfo {
        private List<Annotation> annotations = Lists.newArrayList();
        private final Command commandAnnotation;
        public Object instance;
        private final Method method;
        private final Map<Integer, InjectedCommandArgument> methodArguments = Maps.newHashMap();
        public boolean serverCommand;
        private Collection<String> valueFlags;

        public CommandInfo(Command commandAnnotation, Method method) {
            this.commandAnnotation = commandAnnotation;
            this.method = method;
        }

        public void addArgAnnotation(int idx, Class<?> paramType, Arg arg) {
            this.methodArguments.put(idx, new InjectedCommandArgument(injector, paramType, arg));
        }

        public void addFlagAnnotation(int idx, Class<?> paramType, Flag flag) {
            this.methodArguments.put(idx, new InjectedCommandArgument(injector, paramType, flag));
        }

        private Collection<String> calculateValueFlags() {
            valueFlags = new HashSet<>();
            for (InjectedCommandArgument instance : methodArguments.values()) {
                instance.getValueFlag().ifPresent(flag -> valueFlags.add(flag));
            }
            valueFlags.addAll(Arrays.asList(commandAnnotation.valueFlags()));
            return valueFlags;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            CommandInfo other = (CommandInfo) obj;
            if (!Objects.equals(commandAnnotation, other.commandAnnotation))
                return false;
            return true;
        }

        public Collection<? extends String> getArgTabCompletions(CommandContext args, CommandSender sender, int index) {
            List<String> completions = Lists.newArrayList();
            for (InjectedCommandArgument instance : methodArguments.values()) {
                if (instance.matches(index)) {
                    String needle = index < args.argsLength() ? args.getString(index) : "";
                    completions.addAll(Collections2.filter(instance.getTabCompletions(args, sender),
                            s -> needle.isEmpty() || s.toLowerCase().startsWith(needle.toLowerCase())));
                }
            }
            return completions;
        }

        public Command getCommandAnnotation() {
            return commandAnnotation;
        }

        public Collection<String> getFlagTabCompletions(CommandContext args, CommandSender sender, String flag) {
            List<String> completions = Lists.newArrayList();
            for (InjectedCommandArgument instance : methodArguments.values()) {
                if (instance.matches(flag)) {
                    String needle = args.getFlag(flag, "");
                    completions.addAll(Collections2.filter(instance.getTabCompletions(args, sender),
                            s -> needle.isEmpty() || s.toLowerCase().startsWith(needle.toLowerCase())));
                }
            }
            return completions;
        }

        @Override
        public int hashCode() {
            return 31 + (commandAnnotation == null ? 0 : commandAnnotation.hashCode());
        }

        public Collection<String> valueFlags() {
            return valueFlags == null ? calculateValueFlags() : valueFlags;
        }
    }

    private static class InjectedCommandArgument {
        private final String[] completions;
        private CompletionsProvider completionsProvider;
        private final String defaultValue;
        private int index = -1;
        private final String[] names;
        private final Class<?> paramType;
        private String permission;
        private FlagValidator<?> validator;

        public InjectedCommandArgument(Injector injector, Class<?> paramType, Arg arg) {
            this.paramType = paramType;
            this.names = new String[] {};
            this.index = arg.value();
            this.completions = arg.completions();
            this.defaultValue = arg.defValue().isEmpty() ? null : arg.defValue();
            if (arg.validator() != FlagValidator.Identity.class) {
                this.validator = (FlagValidator<?>) injector.getInstance(arg.validator());
            }
            if (arg.completionsProvider() != CompletionsProvider.Identity.class) {
                this.completionsProvider = (CompletionsProvider) injector.getInstance(arg.completionsProvider());
            }
        }

        public InjectedCommandArgument(Injector injector, Class<?> paramType, Flag flag) {
            this.paramType = paramType;
            this.names = flag.value();
            for (int i = 0; i < this.names.length; i++) {
                this.names[i] = this.names[i].toLowerCase(Locale.ROOT);
            }
            this.permission = flag.permission().isEmpty() ? null : flag.permission();
            this.completions = flag.completions();
            this.defaultValue = flag.defValue().isEmpty() ? null : flag.defValue();
            if (flag.validator() != FlagValidator.Identity.class) {
                this.validator = (FlagValidator<?>) injector.getInstance(flag.validator());
            }
            if (flag.completionsProvider() != CompletionsProvider.Identity.class) {
                this.completionsProvider = (CompletionsProvider) injector.getInstance(flag.completionsProvider());
            }
        }

        public Object getInput(CommandContext context) {
            if (names.length > 0) {
                String flag = names[0];
                Object val = context.getFlag(flag, defaultValue);
                if (val == null && names.length > 1 && !names[1].isEmpty()) {
                    val = context.getFlag(names[1], defaultValue);
                }
                return val;
            } else
                return context.getString(index, defaultValue);
        }

        @SuppressWarnings("rawtypes")
        private Collection<String> getTabCompletions(CommandContext args, CommandSender sender) {
            if (permission != null && !sender.hasPermission(permission))
                return Collections.emptyList();

            if (completionsProvider != null)
                return completionsProvider.getCompletions(args, sender,
                        CitizensAPI.getDefaultNPCSelector().getSelected(sender));

            if (completions.length > 0)
                return Arrays.asList(completions);
            if (SpigotUtil.isRegistryKeyed(paramType)) {
                return Bukkit.getRegistry((Class<? extends Keyed>) paramType).stream().map(Keyed::getKey)
                        .map(NamespacedKey::getKey).collect(Collectors.toList());
            } else if (Enum.class.isAssignableFrom(paramType)) {
                Enum[] constants = (Enum[]) paramType.getEnumConstants();
                return Lists.transform(Arrays.asList(constants), Enum::name);
            } else if (paramType == boolean.class || paramType == Boolean.class)
                return Arrays.asList("true", "false");
            return Collections.emptyList();
        }

        public Optional<String> getValueFlag() {
            return names.length == 0 ? Optional.empty() : Optional.of(names[0]);
        }

        public boolean matches(int index) {
            return this.index == index;
        }

        public boolean matches(String flag) {
            return names.length > 0
                    && (names[0].equalsIgnoreCase(flag) || names.length > 1 && names[1].equalsIgnoreCase(flag));
        }
    }

    private static String capitalize(Object string) {
        String capitalize = string.toString();
        return capitalize.length() == 0 ? "" : Character.toUpperCase(capitalize.charAt(0)) + capitalize.substring(1);
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

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
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

    private static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase(Locale.ROOT))
                    || toMatch.equals("item") && check.name().equals("DROPPED_ITEM"))
                return check; // check for an exact match first
        }
        for (T check : values) {
            String name = check.name().toLowerCase(Locale.ROOT);
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch))
                return check;
        }
        return null;
    }

    private static final String COMMAND_FORMAT = "<green>/{{%s%s <green>- [[%s";

    // Logger for general errors.
    private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());

    private static boolean SUPPORTS_KEYED = false;

    static {
        try {
            Class.forName("org.bukkit.Keyed");
            SUPPORTS_KEYED = true;
        } catch (ClassNotFoundException e) {
        }
    }
}
