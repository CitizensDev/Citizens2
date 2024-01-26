package net.citizensnpcs.trait;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCCommandDispatchEvent;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.trait.shop.ExperienceAction;
import net.citizensnpcs.trait.shop.ItemAction;
import net.citizensnpcs.trait.shop.MoneyAction;
import net.citizensnpcs.trait.shop.NPCShopAction;
import net.citizensnpcs.trait.shop.NPCShopAction.Transaction;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

@TraitName("commandtrait")
public class CommandTrait extends Trait {
    @Persist(keyType = Integer.class)
    @DelegatePersistence(NPCCommandPersister.class)
    private final Map<Integer, NPCCommand> commands = Maps.newHashMap();
    @Persist
    private double cost = -1;
    @Persist
    private final Map<CommandTraitError, String> customErrorMessages = Maps.newEnumMap(CommandTraitError.class);
    private final Map<String, Set<CommandTraitError>> executionErrors = Maps.newHashMap();
    @Persist
    private ExecutionMode executionMode = ExecutionMode.LINEAR;
    @Persist
    private int experienceCost = -1;
    @Persist(valueType = Long.class)
    private final Map<String, Long> globalCooldowns = Maps.newHashMap();
    @Persist
    private boolean hideErrorMessages;
    @Persist
    private final List<ItemStack> itemRequirements = Lists.newArrayList();
    @Persist
    private boolean persistSequence = false;
    @Persist(keyType = UUID.class, reify = true, value = "cooldowns")
    private final Map<UUID, PlayerNPCCommand> playerTracking = Maps.newHashMap();
    @Persist
    private final List<String> temporaryPermissions = Lists.newArrayList();

    public CommandTrait() {
        super("commandtrait");
    }

    public int addCommand(NPCCommandBuilder builder) {
        int id = getNewId();
        commands.put(id, builder.build(id));
        return id;
    }

    private Transaction chargeCommandCosts(Player player, Hand hand, NPCCommand command) {
        NPCShopAction action = null;
        if (player.hasPermission("citizens.npc.command.ignoreerrors.*"))
            return Transaction.success();
        if (cost > 0 && !player.hasPermission("citizens.npc.command.ignoreerrors.cost")) {
            action = new MoneyAction(cost);
            if (!action.take(player, 1).isPossible()) {
                sendErrorMessage(player, CommandTraitError.MISSING_MONEY, null, cost);
            }
        }
        if (experienceCost > 0 && !player.hasPermission("citizens.npc.command.ignoreerrors.expcost")) {
            action = new ExperienceAction(experienceCost);
            if (!action.take(player, 1).isPossible()) {
                sendErrorMessage(player, CommandTraitError.MISSING_EXPERIENCE, null, experienceCost);
            }
        }
        if (itemRequirements.size() > 0 && !player.hasPermission("citizens.npc.command.ignoreerrors.itemcost")) {
            action = new ItemAction(itemRequirements);
            if (!action.take(player, 1).isPossible()) {
                ItemStack stack = itemRequirements.get(0);
                sendErrorMessage(player, CommandTraitError.MISSING_ITEM, null, Util.prettyEnum(stack.getType()),
                        stack.getAmount());
            }
        }
        if (command.cost != -1 && !player.hasPermission("citizens.npc.command.ignoreerrors.cost")) {
            action = new MoneyAction(command.cost);
            if (!action.take(player, 1).isPossible()) {
                sendErrorMessage(player, CommandTraitError.MISSING_MONEY, null, command.cost);
            }
        }
        if (command.experienceCost != -1 && !player.hasPermission("citizens.npc.command.ignoreerrors.expcost")) {
            action = new ExperienceAction(command.experienceCost);
            if (!action.take(player, 1).isPossible()) {
                sendErrorMessage(player, CommandTraitError.MISSING_EXPERIENCE, null, command.experienceCost);
            }
        }
        if (command.itemCost != null && command.itemCost.size() > 0
                && !player.hasPermission("citizens.npc.command.ignoreerrors.itemcost")) {
            action = new ItemAction(command.itemCost);
            if (!action.take(player, 1).isPossible()) {
                ItemStack stack = command.itemCost.get(0);
                sendErrorMessage(player, CommandTraitError.MISSING_ITEM, null, Util.prettyEnum(stack.getType()),
                        stack.getAmount());
            }
        }
        return action == null ? Transaction.success() : action.take(player, 1);
    }

    public void clear() {
        commands.clear();
    }

    public void clearHistory(CommandTraitError which, String raw) {
        if (which == CommandTraitError.ON_GLOBAL_COOLDOWN && raw != null) {
            globalCooldowns.remove(BaseEncoding.base64().encode(raw.getBytes()));
            return;
        }
        Player who = null;
        if (raw != null) {
            who = Bukkit.getPlayerExact(raw);
            if (who == null) {
                who = Bukkit.getPlayer(UUID.fromString(raw));
            }
        }
        Collection<PlayerNPCCommand> toClear = Lists.newArrayList();
        if (who != null) {
            toClear.add(playerTracking.get(who.getUniqueId()));
        } else {
            toClear.addAll(playerTracking.values());
        }
        switch (which) {
            case MAXIMUM_TIMES_USED:
                for (PlayerNPCCommand tracked : toClear) {
                    tracked.nUsed.clear();
                }

                break;
            case ON_COOLDOWN:
                for (PlayerNPCCommand tracked : toClear) {
                    tracked.lastUsed.clear();
                }

                break;
            case ON_GLOBAL_COOLDOWN:
                globalCooldowns.clear();
                break;
            default:
                return;
        }
    }

    /**
     * Send a brief description of the current state of the trait to the supplied {@link CommandSender}.
     */
    public void describe(CommandSender sender) {
        List<NPCCommand> left = Lists.newArrayList();
        List<NPCCommand> right = Lists.newArrayList();
        for (NPCCommand command : commands.values()) {
            if (command.hand == Hand.LEFT || command.hand == Hand.SHIFT_LEFT || command.hand == Hand.BOTH) {
                left.add(command);
            }
            if (command.hand == Hand.RIGHT || command.hand == Hand.SHIFT_RIGHT || command.hand == Hand.BOTH) {
                right.add(command);
            }
        }
        List<String> outputList = Lists.newArrayList();
        if (cost > 0) {
            outputList.add("Cost: " + StringHelper.wrap(cost));
        }
        if (experienceCost > 0) {
            outputList.add("XP cost: " + StringHelper.wrap(experienceCost));
        }
        if (left.size() > 0) {
            outputList.add(Messaging.tr(Messages.COMMAND_LEFT_HAND_HEADER));
            for (NPCCommand command : left) {
                outputList.add(describe(command));
            }
        }
        if (right.size() > 0) {
            outputList.add(Messaging.tr(Messages.COMMAND_RIGHT_HAND_HEADER));
            for (NPCCommand command : right) {
                outputList.add(describe(command));
            }
        }
        if (outputList.isEmpty()) {
            outputList.add(Messaging.tr(Messages.COMMAND_NO_COMMANDS_ADDED));
        } else {
            outputList.add(0, executionMode.toString());
        }
        StringBuilder output = new StringBuilder();
        for (String item : outputList) {
            output.append(item);
            output.append(" ");
        }
        Messaging.send(sender, output.toString().trim());
    }

    private String describe(NPCCommand command) {
        String output = Messaging.tr(Messages.COMMAND_DESCRIBE_TEMPLATE, command.command,
                StringHelper.wrap(command.cooldown != 0 ? command.cooldown
                        : Setting.NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN.asSeconds()),
                StringHelper.wrap(hasCost(command.id) ? command.cost : "default"),
                StringHelper.wrap(hasExperienceCost(command.id) ? command.experienceCost : "default"), command.id);
        if (command.globalCooldown > 0) {
            output += "[global " + StringHelper.wrap(command.globalCooldown) + "s]";
        }
        if (command.delay > 0) {
            output += "[delay " + StringHelper.wrap(command.delay) + "t]";
        }
        if (command.n > 0) {
            output += "[" + StringHelper.wrap(command.n) + " uses]";
        }
        if (command.op) {
            output += " -o";
        }
        if (command.player) {
            output += " -p";
        }
        return output;
    }

    public void dispatch(Player player, Hand handIn) {
        Hand hand = player.isSneaking()
                ? handIn == CommandTrait.Hand.LEFT ? CommandTrait.Hand.SHIFT_LEFT : CommandTrait.Hand.SHIFT_RIGHT
                : handIn;
        NPCCommandDispatchEvent event = new NPCCommandDispatchEvent(npc, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        Runnable task = new Runnable() {
            Boolean charged = null;

            @Override
            public void run() {
                List<NPCCommand> commandList = Lists.newArrayList(Iterables.filter(commands.values(),
                        command -> (command.hand == hand || command.hand == Hand.BOTH)));
                if (executionMode == ExecutionMode.RANDOM) {
                    if (commandList.size() > 0) {
                        runCommand(player, hand, commandList.get(Util.getFastRandom().nextInt(commandList.size())));
                    }
                    return;
                }
                int max = -1;
                if (executionMode == ExecutionMode.SEQUENTIAL) {
                    Collections.sort(commandList, Comparator.comparing(o1 -> o1.id));
                    max = commandList.size() > 0 ? commandList.get(commandList.size() - 1).id : -1;
                }
                if (executionMode == ExecutionMode.LINEAR) {
                    executionErrors.put(player.getUniqueId().toString(), EnumSet.noneOf(CommandTraitError.class));
                }
                for (NPCCommand command : commandList) {
                    PlayerNPCCommand info = null;
                    if (executionMode == ExecutionMode.SEQUENTIAL
                            && (info = playerTracking.get(player.getUniqueId())) != null) {
                        if (info.lastUsedHand != hand) {
                            info.lastUsedHand = hand;
                            info.lastUsedId = -1;
                        }
                        if (command.id <= info.lastUsedId) {
                            if (info.lastUsedId != max)
                                continue;
                            info.lastUsedId = -1;
                        }
                    }
                    runCommand(player, hand, command);
                    if (executionMode == ExecutionMode.SEQUENTIAL || (charged != null && !charged))
                        break;
                }
            }

            private void runCommand(Player player, Hand hand, NPCCommand command) {
                Runnable runnable = () -> {
                    PlayerNPCCommand info = playerTracking.get(player.getUniqueId());
                    if (info == null && (executionMode == ExecutionMode.SEQUENTIAL
                            || PlayerNPCCommand.requiresTracking(command))) {
                        playerTracking.put(player.getUniqueId(), info = new PlayerNPCCommand());
                    }
                    Transaction charge = null;
                    if (charged == null) {
                        charge = chargeCommandCosts(player, hand, command);
                        if (!charge.isPossible()) {
                            charged = false;
                            return;
                        }
                    }
                    if (info != null && !info.canUse(CommandTrait.this, player, hand, command))
                        return;

                    if (charged == null) {
                        charge.run();
                    }
                    if (temporaryPermissions.size() > 0) {
                        PermissionAttachment attachment = player.addAttachment(CitizensAPI.getPlugin());
                        if (attachment != null) {
                            for (String permission : temporaryPermissions) {
                                attachment.setPermission(permission, true);
                            }
                            command.run(npc, player);
                            attachment.remove();
                            return;
                        }
                    }
                    command.run(npc, player);
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

    public String fillPlaceholder(CommandSender sender, String input) {
        return null;
    }

    public double getCost() {
        return cost;
    }

    public double getCost(int id) {
        return commands.get(id).cost;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public float getExperienceCost() {
        return experienceCost;
    }

    public int getExperienceCost(int id) {
        return commands.get(id).experienceCost;
    }

    public List<ItemStack> getItemCost(int id) {
        return commands.get(id).itemCost;
    }

    private int getNewId() {
        int i = 0;
        while (commands.containsKey(i)) {
            i++;
        }
        return i;
    }

    public boolean hasCommandId(int id) {
        return commands.containsKey(id);
    }

    public boolean hasCost(int id) {
        return commands.get(id).cost != -1;
    }

    public boolean hasExperienceCost(int id) {
        return commands.get(id).experienceCost != -1;
    }

    public boolean hasItemCost(int id) {
        return !commands.get(id).itemCost.isEmpty();
    }

    public boolean isHideErrorMessages() {
        return hideErrorMessages;
    }

    public boolean persistSequence() {
        return persistSequence;
    }

    public void removeCommandById(int id) {
        commands.remove(id);
    }

    @Override
    public void save(DataKey key) {
        Collection<NPCCommand> commands = this.commands.values();
        for (Iterator<PlayerNPCCommand> itr = playerTracking.values().iterator(); itr.hasNext();) {
            PlayerNPCCommand playerCommand = itr.next();
            playerCommand.prune(globalCooldowns, commands);
            if (playerCommand.lastUsed.isEmpty() && playerCommand.nUsed.isEmpty()
                    && (!persistSequence || playerCommand.lastUsedId == -1)) {
                itr.remove();
            }
        }
    }

    private void sendErrorMessage(Player player, CommandTraitError msg, Function<String, String> transform,
            Object... objects) {
        if (hideErrorMessages)
            return;
        Set<CommandTraitError> sent = executionErrors.get(player.getUniqueId().toString());
        if (sent != null) {
            if (sent.contains(msg))
                return;
            sent.add(msg);
        }
        String messageRaw = customErrorMessages.getOrDefault(msg, msg.setting.asString());
        if (transform != null) {
            messageRaw = transform.apply(messageRaw);
        }
        if (messageRaw != null && messageRaw.trim().length() > 0) {
            Messaging.send(player, Translator.format(messageRaw, objects));
        }
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setCost(double cost, int id) {
        commands.get(id).cost = cost;
    }

    public void setCustomErrorMessage(CommandTraitError which, String message) {
        customErrorMessages.put(which, message);
    }

    public void setExecutionMode(ExecutionMode mode) {
        executionMode = mode;
    }

    public void setExperienceCost(int experienceCost) {
        this.experienceCost = experienceCost;
    }

    public void setExperienceCost(int experienceCost, int id) {
        commands.get(id).experienceCost = experienceCost;
    }

    public void setHideErrorMessages(boolean hide) {
        hideErrorMessages = hide;
    }

    public void setItemCost(List<ItemStack> itemCost, int id) {
        commands.get(id).itemCost.clear();
        commands.get(id).itemCost.addAll(itemCost);
    }

    public void setPersistSequence(boolean persistSequence) {
        this.persistSequence = persistSequence;
    }

    public void setTemporaryPermissions(List<String> permissions) {
        temporaryPermissions.clear();
        temporaryPermissions.addAll(permissions);
    }

    public enum CommandTraitError {
        MAXIMUM_TIMES_USED(Setting.NPC_COMMAND_MAXIMUM_TIMES_USED_MESSAGE),
        MISSING_EXPERIENCE(Setting.NPC_COMMAND_NOT_ENOUGH_EXPERIENCE_MESSAGE),
        MISSING_ITEM(Setting.NPC_COMMAND_MISSING_ITEM_MESSAGE),
        MISSING_MONEY(Setting.NPC_COMMAND_NOT_ENOUGH_MONEY_MESSAGE),
        NO_PERMISSION(Setting.NPC_COMMAND_NO_PERMISSION_MESSAGE),
        ON_COOLDOWN(Setting.NPC_COMMAND_ON_COOLDOWN_MESSAGE),
        ON_GLOBAL_COOLDOWN(Setting.NPC_COMMAND_ON_GLOBAL_COOLDOWN_MESSAGE);

        private final Setting setting;

        CommandTraitError(Setting setting) {
            this.setting = setting;
        }
    }

    public enum ExecutionMode {
        LINEAR,
        RANDOM,
        SEQUENTIAL;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public static enum Hand {
        BOTH,
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT;
    }

    @Menu(title = "Drag items for requirements", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class ItemRequirementGUI extends InventoryMenuPage {
        private int id = -1;
        private Inventory inventory;
        private CommandTrait trait;

        private ItemRequirementGUI() {
            throw new UnsupportedOperationException();
        }

        public ItemRequirementGUI(CommandTrait trait) {
            this.trait = trait;
        }

        public ItemRequirementGUI(CommandTrait trait, int id) {
            this.trait = trait;
            this.id = id;
        }

        @Override
        public void initialise(MenuContext ctx) {
            inventory = ctx.getInventory();
            if (id == -1) {
                for (ItemStack stack : trait.itemRequirements) {
                    inventory.addItem(stack.clone());
                }
            } else {
                for (ItemStack stack : trait.commands.get(id).itemCost) {
                    inventory.addItem(stack.clone());
                }
            }
        }

        @Override
        public void onClick(InventoryMenuSlot slot, InventoryClickEvent event) {
            event.setCancelled(false);
        }

        @Override
        public void onClose(HumanEntity player) {
            List<ItemStack> requirements = Lists.newArrayList();
            for (ItemStack stack : inventory.getContents()) {
                if (stack != null && stack.getType() != Material.AIR) {
                    requirements.add(stack);
                }
            }
            if (id == -1) {
                trait.itemRequirements.clear();
                trait.itemRequirements.addAll(requirements);
            } else {
                trait.setItemCost(requirements, id);
            }
        }
    }

    private static class NPCCommand {
        String command;
        int cooldown;
        double cost;
        int delay;
        int experienceCost;
        int globalCooldown;
        Hand hand;
        int id;
        List<ItemStack> itemCost;
        String key;
        int n;
        boolean op;
        List<String> perms;
        boolean player;

        public NPCCommand(int id, String command, Hand hand, boolean player, boolean op, int cooldown,
                List<String> perms, int n, int delay, int globalCooldown, double cost, int experienceCost,
                List<ItemStack> itemCost) {
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
            this.cost = cost;
            this.experienceCost = experienceCost;
            this.itemCost = itemCost;
        }

        public String getEncodedKey() {
            if (key != null)
                return key;
            return key = BaseEncoding.base64().encode(command.getBytes());
        }

        public void run(NPC npc, Player clicker) {
            Util.runCommand(npc, clicker, command, op, player);
        }
    }

    public static class NPCCommandBuilder {
        String command;
        int cooldown;
        double cost = -1;
        int delay;
        int experienceCost = -1;
        int globalCooldown;
        Hand hand;
        List<ItemStack> itemCost = Lists.newArrayList();
        int n = -1;
        boolean op;
        List<String> perms = Lists.newArrayList();
        boolean player;

        public NPCCommandBuilder(String command, Hand hand) {
            this.command = command;
            this.hand = hand;
        }

        public NPCCommandBuilder addPerm(String permission) {
            perms.add(permission);
            return this;
        }

        public NPCCommandBuilder addPerms(List<String> perms) {
            this.perms.addAll(perms);
            return this;
        }

        private NPCCommand build(int id) {
            return new NPCCommand(id, command, hand, player, op, cooldown, perms, n, delay, globalCooldown, cost,
                    experienceCost, itemCost);
        }

        public NPCCommandBuilder command(String command) {
            this.command = command;
            return this;
        }

        public NPCCommandBuilder cooldown(Duration cd) {
            return cooldown(Util.convert(TimeUnit.SECONDS, cd));
        }

        public NPCCommandBuilder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public NPCCommandBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public NPCCommandBuilder delay(Duration delay) {
            this.delay = Util.toTicks(delay);
            return this;
        }

        public NPCCommandBuilder experienceCost(int experienceCost) {
            this.experienceCost = experienceCost;
            return this;
        }

        public NPCCommandBuilder globalCooldown(Duration cd) {
            return globalCooldown(Util.convert(TimeUnit.SECONDS, cd));
        }

        public NPCCommandBuilder globalCooldown(int cooldown) {
            globalCooldown = cooldown;
            return this;
        }

        public NPCCommandBuilder itemCost(List<ItemStack> itemCost) {
            this.itemCost = itemCost;
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
            List<ItemStack> items = Lists.newArrayList();
            for (DataKey key : root.getRelative("itemCost").getIntegerSubKeys()) {
                items.add(ItemStorage.loadItemStack(key));
            }
            double cost = root.keyExists("cost") ? root.getDouble("cost") : -1;
            int exp = root.keyExists("experienceCost") ? root.getInt("experienceCost") : -1;
            return new NPCCommand(Integer.parseInt(root.name()), root.getString("command"),
                    Hand.valueOf(root.getString("hand")), Boolean.parseBoolean(root.getString("player")),
                    Boolean.parseBoolean(root.getString("op")), root.getInt("cooldown"), perms, root.getInt("n"),
                    root.getInt("delay"), root.getInt("globalcooldown"), cost, exp, items);
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
            root.setDouble("cost", instance.cost);
            root.setInt("experienceCost", instance.experienceCost);
            for (int i = 0; i < instance.itemCost.size(); i++) {
                ItemStorage.saveItem(root.getRelative("itemCost." + i), instance.itemCost.get(i));
            }
        }
    }

    private static class PlayerNPCCommand {
        @Persist(valueType = Long.class)
        Map<String, Long> lastUsed = Maps.newHashMap();
        @Persist
        Hand lastUsedHand;
        @Persist
        int lastUsedId = -1;
        @Persist
        Map<String, Integer> nUsed = Maps.newHashMap();

        public PlayerNPCCommand() {
        }

        public boolean canUse(CommandTrait trait, Player player, Hand hand, NPCCommand command) {
            for (String perm : command.perms) {
                if (!player.hasPermission(perm)) {
                    trait.sendErrorMessage(player, CommandTraitError.NO_PERMISSION, null);
                    return false;
                }
            }
            long globalDelay = Setting.NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN.asSeconds();
            long currentTimeSec = System.currentTimeMillis() / 1000;
            String commandKey = command.getEncodedKey();
            if (!player.hasPermission("citizens.npc.command.ignoreerrors.cooldown")
                    && lastUsed.containsKey(commandKey)) {
                long deadline = ((Number) lastUsed.get(commandKey)).longValue()
                        + (command.cooldown != 0 ? command.cooldown : globalDelay);
                if (currentTimeSec < deadline) {
                    long seconds = deadline - currentTimeSec;
                    trait.sendErrorMessage(player, CommandTraitError.ON_COOLDOWN,
                            new TimeVariableFormatter(seconds, TimeUnit.SECONDS), seconds);
                    return false;
                }
                lastUsed.remove(commandKey);
            }
            if (!player.hasPermission("citizens.npc.command.ignoreerrors.globalcooldown") && command.globalCooldown > 0
                    && trait.globalCooldowns.containsKey(commandKey)) {
                long deadline = ((Number) trait.globalCooldowns.get(commandKey)).longValue() + command.globalCooldown;
                if (currentTimeSec < deadline) {
                    long seconds = deadline - currentTimeSec;
                    trait.sendErrorMessage(player, CommandTraitError.ON_GLOBAL_COOLDOWN,
                            new TimeVariableFormatter(seconds, TimeUnit.SECONDS), seconds);
                    return false;
                }
                trait.globalCooldowns.remove(commandKey);
            }
            int timesUsed = nUsed.getOrDefault(commandKey, 0);
            if (!player.hasPermission("citizens.npc.command.ignoreerrors.nused") && command.n > 0
                    && command.n <= timesUsed) {
                trait.sendErrorMessage(player, CommandTraitError.MAXIMUM_TIMES_USED, null, command.n);
                return false;
            }
            if (command.cooldown > 0 || globalDelay > 0) {
                lastUsed.put(commandKey, currentTimeSec);
            }
            if (command.globalCooldown > 0) {
                trait.globalCooldowns.put(commandKey, currentTimeSec);
            }
            if (command.n > 0) {
                nUsed.put(commandKey, timesUsed + 1);
            }
            lastUsedId = command.id;
            lastUsedHand = hand;
            return true;
        }

        public void prune(Map<String, Long> globalCooldowns, Collection<NPCCommand> commands) {
            long currentTimeSec = System.currentTimeMillis() / 1000;
            Set<String> commandKeys = Sets.newHashSet();
            for (NPCCommand command : commands) {
                String commandKey = command.getEncodedKey();
                commandKeys.add(commandKey);
                Number number = lastUsed.get(commandKey);
                if (number != null && number.longValue() + (command.cooldown != 0 ? command.cooldown
                        : Setting.NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN.asSeconds()) < currentTimeSec) {
                    lastUsed.remove(commandKey);
                }
                if (globalCooldowns != null) {
                    number = globalCooldowns.get(commandKey);
                    if (number != null && number.longValue() + command.globalCooldown < currentTimeSec) {
                        globalCooldowns.remove(commandKey);
                    }
                }
            }
            Set<String> diff = Sets.newHashSet(lastUsed.keySet());
            diff.removeAll(commandKeys);
            for (String key : diff) {
                lastUsed.remove(key);
                nUsed.remove(key);
            }
            if (globalCooldowns != null) {
                diff = Sets.newHashSet(globalCooldowns.keySet());
                diff.removeAll(commandKeys);
                for (String key : diff) {
                    globalCooldowns.remove(key);
                }
            }
        }

        public static boolean requiresTracking(NPCCommand command) {
            return command.globalCooldown > 0 || command.cooldown > 0 || command.n > 0
                    || command.perms != null && command.perms.size() > 0
                    || Setting.NPC_COMMAND_GLOBAL_COMMAND_COOLDOWN.asSeconds() > 0;
        }
    }

    private static class TimeVariableFormatter implements Function<String, String> {
        private final Map<String, String> map = Maps.newHashMapWithExpectedSize(5);

        public TimeVariableFormatter(long source, TimeUnit unit) {
            long seconds = TimeUnit.SECONDS.convert(source, unit);
            long minutes = TimeUnit.MINUTES.convert(source, unit);
            long hours = TimeUnit.HOURS.convert(source, unit);
            long days = TimeUnit.DAYS.convert(source, unit);
            map.put("seconds", "" + seconds);
            map.put("seconds_over", "" + (seconds - TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES)));
            map.put("minutes", "" + minutes);
            map.put("minutes_over", "" + (minutes - TimeUnit.MINUTES.convert(hours, TimeUnit.HOURS)));
            map.put("hours", "" + hours);
            map.put("hours_over", "" + (hours - TimeUnit.HOURS.convert(days, TimeUnit.DAYS)));
            map.put("days", "" + days);
        }

        @Override
        public String apply(String t) {
            return StrSubstitutor.replace(t, map, "{", "}");
        }
    }
}
