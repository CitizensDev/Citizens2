package net.citizensnpcs.trait;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.event.NPCCommandDispatchEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

@TraitName("commandtrait")
public class CommandTrait extends Trait {
    @Persist
    @DelegatePersistence(NPCCommandPersister.class)
    private final Map<String, NPCCommand> commands = Maps.newHashMap();
    @Persist
    @DelegatePersistence(PlayerNPCCommandPersister.class)
    private final Map<String, PlayerNPCCommand> cooldowns = Maps.newHashMap();
    @Persist
    private double cost = -1;
    @Persist
    private ExecutionMode executionMode = ExecutionMode.LINEAR;
    @Persist
    private final List<String> temporaryPermissions = Lists.newArrayList();

    public CommandTrait() {
        super("commandtrait");
    }

    public int addCommand(NPCCommandBuilder builder) {
        int id = getNewId();
        commands.put(String.valueOf(id), builder.build(id));
        return id;
    }

    private boolean checkPreconditions(Player player, Hand hand) {
        if (cost > 0) {
            try {
                RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager()
                        .getRegistration(Economy.class);
                if (provider != null && provider.getProvider() != null) {
                    Economy economy = provider.getProvider();
                    if (!economy.has(player, cost))
                        return false;
                    economy.withdrawPlayer(player, cost);
                }
            } catch (NoClassDefFoundError e) {
                Messaging.severe("Unable to find Vault when checking command cost - is it installed?");
            }
        }
        return true;
    }

    /**
     * Send a brief description of the current state of the trait to the supplied {@link CommandSender}.
     */
    public void describe(CommandSender sender) {
        List<NPCCommand> left = Lists.newArrayList();
        List<NPCCommand> right = Lists.newArrayList();
        for (NPCCommand command : commands.values()) {
            if (command.hand == Hand.LEFT || command.hand == Hand.BOTH) {
                left.add(command);
            }
            if (command.hand == Hand.RIGHT || command.hand == Hand.BOTH) {
                right.add(command);
            }
        }
        String output = "";
        if (left.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_LEFT_HAND_HEADER);
            for (NPCCommand command : left) {
                output += describe(command);
            }
        }
        if (right.size() > 0) {
            output += Messaging.tr(Messages.COMMAND_RIGHT_HAND_HEADER);
            for (NPCCommand command : right) {
                output += describe(command);
            }
        }
        if (output.isEmpty()) {
            output = Messaging.tr(Messages.COMMAND_NO_COMMANDS_ADDED);
        }
        Messaging.send(sender, output);
    }

    private String describe(NPCCommand command) {
        String output = "<br>    - [" + StringHelper.wrap(command.id) + "]: " + command.command + " ["
                + StringHelper.wrap(command.cooldown + "s") + "]";
        if (command.op) {
            output += " -o";
        }
        if (command.player) {
            output += " -p";
        }
        return output;
    }

    public void dispatch(final Player player, final Hand hand) {
        NPCCommandDispatchEvent event = new NPCCommandDispatchEvent(npc, player);
        event.setCancelled(!checkPreconditions(player, hand));
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Runnable task = new Runnable() {
            @Override
            public void run() {
                List<NPCCommand> commandList = Lists
                        .newArrayList(Iterables.filter(commands.values(), new Predicate<NPCCommand>() {
                            @Override
                            public boolean apply(NPCCommand command) {
                                return command.hand == hand || command.hand == Hand.BOTH;
                            }
                        }));
                if (executionMode == ExecutionMode.RANDOM) {
                    if (commandList.size() > 0) {
                        runCommand(player, commandList.get(Util.getFastRandom().nextInt(commandList.size())));
                    }
                    return;
                }
                int max = -1;
                if (executionMode == ExecutionMode.SEQUENTIAL) {
                    Collections.sort(commandList, new Comparator<NPCCommand>() {
                        @Override
                        public int compare(NPCCommand o1, NPCCommand o2) {
                            return Integer.compare(o1.id, o2.id);
                        }
                    });
                    max = commandList.size() > 0 ? commandList.get(commandList.size() - 1).id : -1;
                }
                for (NPCCommand command : commandList) {
                    if (executionMode == ExecutionMode.SEQUENTIAL) {
                        PlayerNPCCommand info = cooldowns.get(player.getUniqueId().toString());
                        if (info != null && command.id <= info.lastUsedId) {
                            if (info.lastUsedId == max) {
                                info.lastUsedId = -1;
                            } else {
                                continue;
                            }
                        }
                    }
                    runCommand(player, command);
                }
            }

            private void runCommand(final Player player, NPCCommand command) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        PlayerNPCCommand info = cooldowns.get(player.getUniqueId().toString());
                        if (info == null && (executionMode == ExecutionMode.SEQUENTIAL
                                || PlayerNPCCommand.requiresTracking(command))) {
                            cooldowns.put(player.getUniqueId().toString(), info = new PlayerNPCCommand());
                        }
                        if (info != null && !info.canUse(player, command)) {
                            return;
                        }
                        PermissionAttachment attachment = player.addAttachment(CitizensAPI.getPlugin());
                        if (temporaryPermissions.size() > 0) {
                            for (String permission : temporaryPermissions) {
                                attachment.setPermission(permission, true);
                            }
                        }
                        command.run(npc, player);
                        attachment.remove();
                    }
                };
                if (command.delay <= 0) {
                    runnable.run();
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), runnable, command.delay);
                }
            }
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), task);
        }
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    private int getNewId() {
        int i = 0;
        while (commands.containsKey(String.valueOf(i))) {
            i++;
        }
        return i;
    }

    public boolean hasCommandId(int id) {
        return commands.containsKey(String.valueOf(id));
    }

    public void removeCommandById(int id) {
        commands.remove(String.valueOf(id));
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setExecutionMode(ExecutionMode mode) {
        this.executionMode = mode;
    }

    public void setTemporaryPermissions(List<String> permissions) {
        temporaryPermissions.clear();
        temporaryPermissions.addAll(permissions);
    }

    public enum ExecutionMode {
        LINEAR,
        RANDOM,
        SEQUENTIAL;
    }

    public static enum Hand {
        BOTH,
        LEFT,
        RIGHT;
    }

    private static class NPCCommand {
        String bungeeServer;
        String command;
        int cooldown;
        int delay;
        Hand hand;
        int id;
        int n;
        boolean op;
        List<String> perms;
        boolean player;

        public NPCCommand(int id, String command, Hand hand, boolean player, boolean op, int cooldown,
                List<String> perms, int n, int delay) {
            this.id = id;
            this.command = command;
            this.hand = hand;
            this.player = player;
            this.op = op;
            this.cooldown = cooldown;
            this.perms = perms;
            this.n = n;
            this.delay = delay;
            List<String> split = Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(command);
            this.bungeeServer = split.size() == 2 && split.get(0).equalsIgnoreCase("server") ? split.get(1) : null;
        }

        public void run(NPC npc, Player clicker) {
            String interpolatedCommand = Placeholders.replace(command, clicker, npc);
            if (Messaging.isDebugging()) {
                Messaging.debug(
                        "Running command " + interpolatedCommand + " on NPC " + npc.getId() + " clicker " + clicker);
            }
            if (!player) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), interpolatedCommand);
                return;
            }
            boolean wasOp = clicker.isOp();
            if (op) {
                clicker.setOp(true);
            }

            if (bungeeServer != null) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(bungeeServer);

                clicker.sendPluginMessage(CitizensAPI.getPlugin(), "BungeeCord", out.toByteArray());
            } else {
                try {
                    clicker.chat("/" + interpolatedCommand);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            if (op) {
                clicker.setOp(wasOp);
            }
        }
    }

    public static class NPCCommandBuilder {
        String command;
        int cooldown;
        int delay;
        Hand hand;
        int n = -1;
        boolean op;
        List<String> perms = Lists.newArrayList();
        boolean player;

        public NPCCommandBuilder(String command, Hand hand) {
            this.command = command;
            this.hand = hand;
        }

        public NPCCommandBuilder addPerm(String permission) {
            this.perms.add(permission);
            return this;
        }

        public NPCCommandBuilder addPerms(List<String> perms) {
            this.perms.addAll(perms);
            return this;
        }

        private NPCCommand build(int id) {
            return new NPCCommand(id, command, hand, player, op, cooldown, perms, n, delay);
        }

        public NPCCommandBuilder command(String command) {
            this.command = command;
            return this;
        }

        public NPCCommandBuilder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public NPCCommandBuilder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public NPCCommandBuilder n(int n) {
            this.n = n;
            return this;
        }

        public NPCCommandBuilder op(boolean op) {
            this.op = op;
            return this;
        }

        public NPCCommandBuilder player(boolean player) {
            this.player = player;
            return this;
        }
    }

    private static class NPCCommandPersister implements Persister<NPCCommand> {
        public NPCCommandPersister() {
        }

        @Override
        public NPCCommand create(DataKey root) {
            List<String> perms = Lists.newArrayList();
            for (DataKey key : root.getRelative("permissions").getIntegerSubKeys()) {
                perms.add(key.getString(""));
            }
            return new NPCCommand(Integer.parseInt(root.name()), root.getString("command"),
                    Hand.valueOf(root.getString("hand")), Boolean.valueOf(root.getString("player")),
                    Boolean.valueOf(root.getString("op")), root.getInt("cooldown"), perms, root.getInt("n"),
                    root.getInt("delay"));
        }

        @Override
        public void save(NPCCommand instance, DataKey root) {
            root.setString("command", instance.command);
            root.setString("hand", instance.hand.name());
            root.setBoolean("player", instance.player);
            root.setBoolean("op", instance.op);
            root.setInt("cooldown", instance.cooldown);
            root.setInt("n", instance.n);
            root.setInt("delay", instance.delay);
            for (int i = 0; i < instance.perms.size(); i++) {
                root.setString("permissions." + i, instance.perms.get(i));
            }
        }
    }

    private static class PlayerNPCCommand {
        @Persist(valueType = Long.class)
        Map<String, Long> lastUsed = Maps.newHashMap();
        @Persist
        int lastUsedId = -1;
        @Persist
        Map<String, Integer> nUsed = Maps.newHashMap();

        public PlayerNPCCommand() {
        }

        public boolean canUse(Player player, NPCCommand command) {
            for (String perm : command.perms) {
                if (!player.hasPermission(perm)) {
                    Messaging.sendErrorTr(player, CommandMessages.NO_PERMISSION);
                    return false;
                }
            }
            long currentTimeSec = System.currentTimeMillis() / 1000;
            if (lastUsed.containsKey(command.command)) {
                if (currentTimeSec < lastUsed.get(command.command) + command.cooldown) {
                    return false;
                }
                lastUsed.remove(command.command);
            }
            int previouslyUsed = nUsed.getOrDefault(command.command, 0);
            if (command.n > 0 && command.n <= previouslyUsed) {
                return false;
            }
            if (command.cooldown > 0) {
                lastUsed.put(command.command, currentTimeSec);
            }
            if (command.n > 0) {
                nUsed.put(command.command, previouslyUsed + 1);
            }
            lastUsedId = command.id;
            return true;
        }

        public static boolean requiresTracking(NPCCommand command) {
            return command.cooldown > 0 || command.n > 0 || (command.perms != null && command.perms.size() > 0);
        }
    }

    private static class PlayerNPCCommandPersister implements Persister<PlayerNPCCommand> {
        public PlayerNPCCommandPersister() {
        }

        @Override
        public PlayerNPCCommand create(DataKey root) {
            return PersistenceLoader.load(PlayerNPCCommand.class, root);
        }

        @Override
        public void save(PlayerNPCCommand instance, DataKey root) {
            PersistenceLoader.save(instance, root);
        }
    }
}