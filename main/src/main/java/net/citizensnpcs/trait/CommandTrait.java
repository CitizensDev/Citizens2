package net.citizensnpcs.trait;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCCommandDispatchEvent;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

@TraitName("commandtrait")
public class CommandTrait extends Trait {
    @Persist
    @DelegatePersistence(NPCCommandPersister.class)
    private final Map<String, NPCCommand> commands = Maps.newHashMap();
    @Persist(reify = true)
    private final Map<String, PlayerNPCCommand> cooldowns = Maps.newHashMap();
    @Persist
    private double cost = -1;
    private final Map<String, Set<CommandTraitMessages>> executionErrors = Maps.newHashMap();
    @Persist
    private ExecutionMode executionMode = ExecutionMode.LINEAR;
    @Persist
    private final Map<String, Long> globalCooldowns = Maps.newHashMap();
    @Persist
    private List<ItemStack> itemRequirements = Lists.newArrayList();
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
                    if (!economy.has(player, cost)) {
                        sendErrorMessage(player, CommandTraitMessages.MISSING_MONEY, cost);
                        return false;
                    }
                    economy.withdrawPlayer(player, cost);
                }
            } catch (NoClassDefFoundError e) {
                Messaging.severe("Unable to find Vault when checking command cost - is it installed?");
            }
        }
        if (itemRequirements.size() > 0) {
            List<ItemStack> req = Lists.newArrayList(itemRequirements);
            Inventory tempInventory = Bukkit.createInventory(null, 54);
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                tempInventory.setItem(i, player.getInventory().getItem(i));
            }
            for (ItemStack stack : req) {
                if (tempInventory.containsAtLeast(stack, stack.getAmount())) {
                    tempInventory.removeItem(stack);
                } else {
                    sendErrorMessage(player, CommandTraitMessages.MISSING_ITEM, Util.prettyEnum(stack.getType()),
                            stack.getAmount());
                    return false;
                }
            }
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                player.getInventory().setItem(i, tempInventory.getItem(i));
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
                if (executionMode == ExecutionMode.LINEAR) {
                    executionErrors.put(player.getUniqueId().toString(), EnumSet.noneOf(CommandTraitMessages.class));
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
                    if (executionMode == ExecutionMode.SEQUENTIAL) {
                        break;
                    }
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
                        if (info != null && !info.canUse(CommandTrait.this, player, command)) {
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

    public double getCost() {
        return cost;
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

    private void sendErrorMessage(Player player, CommandTraitMessages msg, Object... objects) {
        Set<CommandTraitMessages> sent = executionErrors.get(player.getUniqueId().toString());
        if (sent != null) {
            if (sent.contains(msg))
                return;
            sent.add(msg);
        }
        String messageRaw = msg.setting.asString();
        if (messageRaw != null && messageRaw.trim().length() > 0) {
            Messaging.send(player, Translator.format(messageRaw, objects));
        }
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

    private enum CommandTraitMessages {
        MAXIMUM_TIMES_USED(Setting.NPC_COMMAND_MAXIMUM_TIMES_USED_MESSAGE),
        MISSING_ITEM(Setting.NPC_COMMAND_MISSING_ITEM_MESSAGE),
        MISSING_MONEY(Setting.NPC_COMMAND_NOT_ENOUGH_MONEY_MESSAGE),
        NO_PERMISSION(Setting.NPC_COMMAND_NO_PERMISSION_MESSAGE),
        ON_COOLDOWN(Setting.NPC_COMMAND_ON_COOLDOWN_MESSAGE),
        ON_GLOBAL_COOLDOWN(Setting.NPC_COMMAND_ON_GLOBAL_COOLDOWN_MESSAGE);

        private final Setting setting;

        CommandTraitMessages(Setting setting) {
            this.setting = setting;
        }
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

    @Menu(title = "Drag items for requirements", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class ItemRequirementGUI extends InventoryMenuPage {
        private Inventory inventory;
        private int taskId;
        private CommandTrait trait;

        private ItemRequirementGUI() {
            throw new UnsupportedOperationException();
        }

        public ItemRequirementGUI(CommandTrait trait) {
            this.trait = trait;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CitizensAPI.getPlugin(), this, 0, 1);
            this.inventory = ctx.getInventory();
            for (ItemStack stack : trait.itemRequirements) {
                inventory.addItem(stack.clone());
            }
        }

        @Override
        public void onClose(HumanEntity player) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        @Override
        public void run() {
            List<ItemStack> requirements = Lists.newArrayList();
            for (ItemStack stack : inventory.getContents()) {
                if (stack != null && stack.getType() != Material.AIR) {
                    requirements.add(stack);
                }
            }
            this.trait.itemRequirements = requirements;
        }
    }

    private static class NPCCommand {
        String bungeeServer;
        String command;
        int cooldown;
        int delay;
        int globalCooldown;
        Hand hand;
        int id;
        int n;
        boolean op;
        List<String> perms;
        boolean player;

        public NPCCommand(int id, String command, Hand hand, boolean player, boolean op, int cooldown,
                List<String> perms, int n, int delay, int globalCooldown) {
            this.id = id;
            this.command = command;
            this.hand = hand;
            this.player = player;
            this.op = op;
            this.cooldown = cooldown;
            this.perms = perms;
            this.n = n;
            this.delay = delay;
            this.globalCooldown = globalCooldown;
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
        private int globalCooldown;
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
            return new NPCCommand(id, command, hand, player, op, cooldown, perms, n, delay, globalCooldown);
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

        public NPCCommandBuilder globalCooldown(int cooldown) {
            this.globalCooldown = cooldown;
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
                    root.getInt("delay"), root.getInt("globalcooldown"));
        }

        @Override
        public void save(NPCCommand instance, DataKey root) {
            root.setString("command", instance.command);
            root.setString("hand", instance.hand.name());
            root.setBoolean("player", instance.player);
            root.setBoolean("op", instance.op);
            root.setInt("cooldown", instance.cooldown);
            root.setInt("globalcooldown", instance.globalCooldown);
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

        public boolean canUse(CommandTrait trait, Player player, NPCCommand command) {
            for (String perm : command.perms) {
                if (!player.hasPermission(perm)) {
                    trait.sendErrorMessage(player, CommandTraitMessages.NO_PERMISSION);
                    return false;
                }
            }
            long currentTimeSec = System.currentTimeMillis() / 1000;
            String commandKey = BaseEncoding.base64().encode(command.command.getBytes());
            // TODO: remove this in 2.0.28
            for (Map map : Arrays.asList(lastUsed, nUsed)) {
                if (map.containsKey(command)) {
                    Object value = map.remove(command);
                    map.put(commandKey, value);
                }
            }
            if (lastUsed.containsKey(commandKey)) {
                if (currentTimeSec < lastUsed.get(commandKey) + command.cooldown) {
                    trait.sendErrorMessage(player, CommandTraitMessages.ON_COOLDOWN,
                            (lastUsed.get(commandKey) + command.cooldown) - currentTimeSec);
                    return false;
                }
                lastUsed.remove(commandKey);
            }
            if (command.globalCooldown > 0 && trait.globalCooldowns.containsKey(commandKey)) {
                long lastUsedSec = trait.globalCooldowns.get(commandKey);
                if (currentTimeSec < lastUsedSec + command.cooldown) {
                    trait.sendErrorMessage(player, CommandTraitMessages.ON_GLOBAL_COOLDOWN,
                            (lastUsedSec + command.cooldown) - currentTimeSec);
                    return false;
                }
                trait.globalCooldowns.remove(commandKey);
            }
            int previouslyUsed = nUsed.getOrDefault(commandKey, 0);
            if (command.n > 0 && command.n <= previouslyUsed) {
                trait.sendErrorMessage(player, CommandTraitMessages.MAXIMUM_TIMES_USED, command.n);
                return false;
            }
            if (command.cooldown > 0) {
                lastUsed.put(commandKey, currentTimeSec);
            }
            if (command.n > 0) {
                nUsed.put(commandKey, previouslyUsed + 1);
            }
            if (command.globalCooldown > 0) {
                trait.globalCooldowns.put(commandKey, currentTimeSec);
            }
            lastUsedId = command.id;
            return true;
        }

        public static boolean requiresTracking(NPCCommand command) {
            return command.cooldown > 0 || command.n > 0 || (command.perms != null && command.perms.size() > 0);
        }
    }
}