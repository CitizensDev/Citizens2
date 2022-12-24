package net.citizensnpcs.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.StoredShops;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.tree.StatusMapper;
import net.citizensnpcs.api.command.Arg;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.RequirementMissingException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.event.CommandSenderCloneNPCEvent;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCTeleportEvent;
import net.citizensnpcs.api.event.PlayerCloneNPCEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.commands.gui.NPCConfigurator;
import net.citizensnpcs.commands.history.CommandHistory;
import net.citizensnpcs.commands.history.CreateNPCHistoryItem;
import net.citizensnpcs.commands.history.RemoveNPCHistoryItem;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.ClickRedirectTrait;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.CommandTrait.CommandTraitError;
import net.citizensnpcs.trait.CommandTrait.ExecutionMode;
import net.citizensnpcs.trait.CommandTrait.ItemRequirementGUI;
import net.citizensnpcs.trait.CommandTrait.NPCCommandBuilder;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.DropsTrait;
import net.citizensnpcs.trait.EnderCrystalTrait;
import net.citizensnpcs.trait.EndermanTrait;
import net.citizensnpcs.trait.FollowTrait;
import net.citizensnpcs.trait.GameModeTrait;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.HologramTrait.HologramDirection;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.MountTrait;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.ScriptTrait;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.ShopTrait;
import net.citizensnpcs.trait.ShopTrait.NPCShop;
import net.citizensnpcs.trait.SkinLayers;
import net.citizensnpcs.trait.SkinLayers.Layer;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WitherTrait;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final CommandHistory history;
    private final NPCSelector selector;
    private final StoredShops shops;
    private final NPCRegistry temporaryRegistry;

    public NPCCommands(Citizens plugin) {
        selector = plugin.getNPCSelector();
        shops = plugin.getShops();
        temporaryRegistry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
        history = new CommandHistory(selector);
    }

    @Command(
            aliases = { "npc" },
            usage = "activationrange [range]",
            desc = "Sets the activation range",
            modifiers = { "activationrange" },
            min = 1,
            max = 2,
            permission = "citizens.npc.activationrange")
    public void activationrange(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Integer range) {
        if (range == null) {
            npc.data().remove(NPC.Metadata.ACTIVATION_RANGE);
        } else {
            npc.data().setPersistent(NPC.Metadata.ACTIVATION_RANGE, range);
        }
        Messaging.sendTr(sender, Messages.ACTIVATION_RANGE_SET, range);
    }

    @Command(
            aliases = { "npc" },
            usage = "age [age] (-l(ock))",
            desc = "Set the age of a NPC",
            help = Messages.COMMAND_AGE_HELP,
            flags = "l",
            modifiers = { "age" },
            min = 1,
            max = 2,
            permission = "citizens.npc.age")
    public void age(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.isSpawned() || (!(npc.getEntity() instanceof Ageable) && !(npc.getEntity() instanceof Zombie)
                && !npc.getEntity().getType().name().equals("TADPOLE")))
            throw new CommandException(Messages.MOBTYPE_CANNOT_BE_AGED, npc.getName());
        Age trait = npc.getOrAddTrait(Age.class);
        boolean toggleLock = args.hasFlag('l');
        if (toggleLock) {
            Messaging.sendTr(sender, trait.toggle() ? Messages.AGE_LOCKED : Messages.AGE_UNLOCKED);
        }
        if (args.argsLength() <= 1) {
            if (!toggleLock) {
                trait.describe(sender);
            }
            return;
        }
        int age = 0;
        try {
            age = args.getInteger(1);
            if (age > 0)
                throw new CommandException(Messages.INVALID_AGE);

            Messaging.sendTr(sender, Messages.AGE_SET_NORMAL, npc.getName(), age);
        } catch (NumberFormatException ex) {
            if (args.getString(1).equalsIgnoreCase("baby")) {
                age = -24000;
                Messaging.sendTr(sender, Messages.AGE_SET_BABY, npc.getName());
            } else if (args.getString(1).equalsIgnoreCase("adult")) {
                age = 0;
                Messaging.sendTr(sender, Messages.AGE_SET_ADULT, npc.getName());
            } else
                throw new CommandException(Messages.INVALID_AGE);
        }

        trait.setAge(age);
    }

    @Command(
            aliases = { "npc" },
            usage = "ai (true|false)",
            desc = "Sets whether the NPC should use vanilla AI",
            modifiers = { "ai" },
            min = 1,
            max = 2,
            permission = "citizens.npc.ai")
    public void ai(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Boolean explicit)
            throws CommandException {
        boolean useAI = explicit == null ? !npc.useMinecraftAI() : explicit;
        npc.setUseMinecraftAI(useAI);
        Messaging.sendTr(sender, useAI ? Messages.USING_MINECRAFT_AI : Messages.NOT_USING_MINECRAFT_AI);
    }

    @Command(
            aliases = { "npc" },
            usage = "anchor (--save [name]|--assume [name]|--remove [name]) (-a) (-c)",
            desc = "Changes/Saves/Lists NPC's location anchor(s)",
            flags = "ac",
            modifiers = { "anchor" },
            min = 1,
            max = 3,
            permission = "citizens.npc.anchor")
    public void anchor(CommandContext args, CommandSender sender, NPC npc, @Flag("save") String save,
            @Flag("assume") String assume, @Flag("remove") String remove) throws CommandException {
        Anchors trait = npc.getOrAddTrait(Anchors.class);
        if (save != null) {
            if (save.isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (args.hasFlag('c')) {
                if (trait.addAnchor(save, args.getSenderTargetBlockLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, save);
            } else {
                if (trait.addAnchor(save, args.getSenderLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, save);
            }
        } else if (assume != null) {
            if (assume.isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            Anchor anchor = trait.getAnchor(assume);
            if (anchor == null)
                throw new CommandException(Messages.ANCHOR_MISSING, assume);
            npc.teleport(anchor.getLocation(), TeleportCause.COMMAND);
        } else if (remove != null) {
            if (remove.isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);
            if (trait.removeAnchor(trait.getAnchor(remove)))
                Messaging.sendTr(sender, Messages.ANCHOR_REMOVED);
            else
                throw new CommandException(Messages.ANCHOR_MISSING, remove);
        } else if (!args.hasFlag('a')) {
            Paginator paginator = new Paginator().header("Anchors").console(sender instanceof ConsoleCommandSender);
            paginator.addLine("Key: [[ID]]  <blue>Name  <red>World  <gray>Location (X,Y,Z)");
            for (int i = 0; i < trait.getAnchors().size(); i++) {
                if (trait.getAnchors().get(i).isLoaded()) {
                    String line = i + "<blue>  " + trait.getAnchors().get(i).getName() + "<yellow>  "
                            + trait.getAnchors().get(i).getLocation().getWorld().getName() + "<gray>  "
                            + trait.getAnchors().get(i).getLocation().getBlockX() + ", "
                            + trait.getAnchors().get(i).getLocation().getBlockY() + ", "
                            + trait.getAnchors().get(i).getLocation().getBlockZ();
                    paginator.addLine(line);
                } else {
                    String[] parts = trait.getAnchors().get(i).getUnloadedValue();
                    String line = i + "<blue>  " + trait.getAnchors().get(i).getName() + "<red>  " + parts[0]
                            + "<gray>  " + parts[1] + ", " + parts[2] + ", " + parts[3] + " <white>(unloaded)";
                    paginator.addLine(line);
                }
            }

            int page = args.getInteger(1, 1);
            if (!paginator.sendPage(sender, page)) {
                throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
            }
        }

        // Assume Player's position
        if (!args.hasFlag('a'))
            return;
        if (sender instanceof ConsoleCommandSender)
            throw new ServerCommandException();
        npc.teleport(args.getSenderLocation(), TeleportCause.COMMAND);
    }

    @Command(
            aliases = { "npc" },
            usage = "armorstand --visible [visible] --small [small] --gravity [gravity] --arms [arms] --baseplate [baseplate] --(body|leftarm|leftleg|rightarm|rightleg)pose [angle x,y,z]",
            desc = "Edit armorstand properties",
            modifiers = { "armorstand" },
            min = 1,
            max = 1,
            valueFlags = { "bodypose", "leftarmpose", "rightarmpose", "leftlegpose", "rightlegpose" },
            permission = "citizens.npc.armorstand")
    @Requirements(selected = true, ownership = true, types = EntityType.ARMOR_STAND)
    public void armorstand(CommandContext args, CommandSender sender, NPC npc, @Flag("visible") Boolean visible,
            @Flag("small") Boolean small, @Flag("gravity") Boolean gravity, @Flag("arms") Boolean arms,
            @Flag("baseplate") Boolean baseplate) throws CommandException {
        ArmorStandTrait trait = npc.getOrAddTrait(ArmorStandTrait.class);
        if (visible != null) {
            trait.setVisible(visible);
        }
        if (small != null) {
            trait.setSmall(small);
        }
        if (gravity != null) {
            trait.setGravity(gravity);
        }
        if (arms != null) {
            trait.setHasArms(arms);
        }
        if (baseplate != null) {
            trait.setHasBaseplate(baseplate);
        }
        ArmorStand ent = (ArmorStand) npc.getEntity();
        if (args.hasValueFlag("bodypose")) {
            ent.setBodyPose(args.parseEulerAngle(args.getFlag("bodypose")));
        }
        if (args.hasValueFlag("leftarmpose")) {
            ent.setLeftArmPose(args.parseEulerAngle(args.getFlag("leftarmpose")));
        }
        if (args.hasValueFlag("leftlegpose")) {
            ent.setLeftLegPose(args.parseEulerAngle(args.getFlag("leftlegpose")));
        }
        if (args.hasValueFlag("rightarmpose")) {
            ent.setRightArmPose(args.parseEulerAngle(args.getFlag("rightarmpose")));
        }
        if (args.hasValueFlag("rightlegpose")) {
            ent.setRightLegPose(args.parseEulerAngle(args.getFlag("rightlegpose")));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "breakblock --location [x,y,z] --radius [radius]",
            desc = "Mine a block at the given location or cursor if not specified",
            modifiers = { "breakblock" },
            min = 1,
            max = 1,
            valueFlags = "location",
            permission = "citizens.npc.breakblock")
    @Requirements(selected = true, ownership = true, livingEntity = true)
    public void breakblock(CommandContext args, CommandSender sender, NPC npc, @Flag("radius") Double radius)
            throws CommandException {
        BlockBreakerConfiguration cfg = new BlockBreakerConfiguration();
        if (radius != null) {
            cfg.radius(radius);
        } else if (Setting.DEFAULT_BLOCK_BREAKER_RADIUS.asDouble() > 0) {
            cfg.radius(Setting.DEFAULT_BLOCK_BREAKER_RADIUS.asDouble());
        }
        BlockBreaker breaker = npc.getBlockBreaker(args.getSenderTargetBlockLocation().getBlock(), cfg);
        npc.getDefaultGoalController().addBehavior(StatusMapper.singleUse(breaker), 1);
    }

    @Command(
            aliases = { "npc" },
            usage = "chunkload (-t(emporary))",
            desc = "Toggle the NPC forcing chunks to stay loaded",
            modifiers = { "chunkload", "cload" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.chunkload")
    @Requirements(selected = true, ownership = true)
    public void chunkload(CommandContext args, CommandSender sender, NPC npc) {
        boolean enabled = !npc.data().get(NPC.KEEP_CHUNK_LOADED_METADATA, Setting.KEEP_CHUNKS_LOADED.asBoolean());
        if (args.hasFlag('t')) {
            npc.data().set(NPC.KEEP_CHUNK_LOADED_METADATA, enabled);
        } else {
            npc.data().setPersistent(NPC.KEEP_CHUNK_LOADED_METADATA, enabled);
        }
        Messaging.sendTr(sender, enabled ? Messages.CHUNKLOAD_SET : Messages.CHUNKLOAD_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "collidable",
            desc = "Toggles an NPC's collidability",
            modifiers = { "collidable", "pushable" },
            min = 1,
            max = 1,
            permission = "citizens.npc.collidable")
    @Requirements(ownership = true, selected = true)
    public void collidable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        npc.data().setPersistent(NPC.Metadata.COLLIDABLE, !npc.data().get(NPC.Metadata.COLLIDABLE, false));
        Messaging.sendTr(sender,
                npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE) ? Messages.COLLIDABLE_SET : Messages.COLLIDABLE_UNSET,
                npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "command|cmd (add [command] | remove [id] | permissions [permissions] | sequential | random | clearerror [type] (name|uuid) | errormsg [type] [msg] | persistsequence [true|false] | (exp|item)cost [cost]) (-s(hift)) (-l[eft]/-r[ight]) (-p[layer] -o[p]), --cooldown --gcooldown [seconds] --delay [ticks] --permissions [perms] --n [max # of uses]",
            desc = "Controls commands which will be run when clicking on an NPC",
            help = Messages.NPC_COMMAND_HELP,
            modifiers = { "command", "cmd" },
            min = 1,
            flags = "lrpos",
            permission = "citizens.npc.command")
    public void command(CommandContext args, CommandSender sender, NPC npc, @Flag("permissions") String permissions,
            @Flag(value = "cooldown", defValue = "0") int cooldown,
            @Flag(value = "gcooldown", defValue = "0") int gcooldown, @Flag(value = "n", defValue = "-1") int n,
            @Flag(value = "delay", defValue = "0") int delay,
            @Arg(
                    value = 1,
                    completions = { "add", "remove", "permissions", "persistsequence", "sequential", "random",
                            "hideerrors", "errormsg", "clearerror", "expcost", "itemcost" }) String action)
            throws CommandException {
        CommandTrait commands = npc.getOrAddTrait(CommandTrait.class);
        if (args.argsLength() == 1) {
            commands.describe(sender);
        } else if (action.equalsIgnoreCase("add")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();
            if (args.hasFlag('o') && !sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            String command = args.getJoinedStrings(2);
            CommandTrait.Hand hand = args.hasFlag('l') && args.hasFlag('r') ? CommandTrait.Hand.BOTH
                    : args.hasFlag('l') ? CommandTrait.Hand.LEFT : CommandTrait.Hand.RIGHT;
            if (args.hasFlag('s') && hand != CommandTrait.Hand.BOTH) {
                hand = hand == CommandTrait.Hand.LEFT ? CommandTrait.Hand.SHIFT_LEFT : CommandTrait.Hand.SHIFT_RIGHT;
            }
            List<String> perms = Lists.newArrayList();
            if (permissions != null) {
                perms.addAll(Arrays.asList(permissions.split(",")));
            }
            try {
                int id = commands.addCommand(new NPCCommandBuilder(command, hand).addPerms(perms)
                        .player(args.hasFlag('p') || args.hasFlag('o')).op(args.hasFlag('o')).cooldown(cooldown)
                        .globalCooldown(gcooldown).n(n).delay(delay));
                Messaging.sendTr(sender, Messages.COMMAND_ADDED, command, id);
            } catch (NumberFormatException ex) {
                throw new CommandException(CommandMessages.INVALID_NUMBER);
            }
        } else if (action.equalsIgnoreCase("clearerror")) {
            if (args.argsLength() < 3)
                throw new CommandException(Messages.NPC_COMMAND_INVALID_ERROR_MESSAGE,
                        Util.listValuesPretty(CommandTraitError.values()));

            CommandTraitError which = Util.matchEnum(CommandTraitError.values(), args.getString(2));
            if (which == null)
                throw new CommandException(Messages.NPC_COMMAND_INVALID_ERROR_MESSAGE,
                        Util.listValuesPretty(CommandTraitError.values()));
            Player player = null;
            if (args.argsLength() > 3) {
                player = Bukkit.getPlayerExact(args.getString(3));
                if (player == null) {
                    player = Bukkit.getPlayer(UUID.fromString(args.getString(3)));
                }
            }
            commands.clearHistory(which, player);
            Messaging.send(sender, Messages.NPC_COMMAND_ERRORS_CLEARED, Util.prettyEnum(which));
        } else if (action.equalsIgnoreCase("sequential")) {
            commands.setExecutionMode(commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? ExecutionMode.LINEAR
                    : ExecutionMode.SEQUENTIAL);
            Messaging.sendTr(sender,
                    commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? Messages.COMMANDS_SEQUENTIAL_SET
                            : Messages.COMMANDS_SEQUENTIAL_UNSET);
        } else if (action.equalsIgnoreCase("persistsequence")) {
            if (args.argsLength() == 2) {
                commands.setPersistSequence(!commands.persistSequence());
            } else {
                commands.setPersistSequence(Boolean.parseBoolean(args.getString(3)));
            }
            Messaging.sendTr(sender, commands.persistSequence() ? Messages.COMMANDS_PERSIST_SEQUENCE_SET
                    : Messages.COMMANDS_PERSIST_SEQUENCE_UNSET);
        } else if (action.equalsIgnoreCase("remove")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();
            int id = args.getInteger(2, -1);
            if (!commands.hasCommandId(id))
                throw new CommandException(Messages.COMMAND_UNKNOWN_COMMAND_ID, id);
            commands.removeCommandById(id);
            Messaging.sendTr(sender, Messages.COMMAND_REMOVED, id);
        } else if (action.equalsIgnoreCase("permissions") || action.equalsIgnoreCase("perms")) {
            if (!sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            List<String> temporaryPermissions = Arrays.asList(args.getSlice(2));
            commands.setTemporaryPermissions(temporaryPermissions);
            Messaging.sendTr(sender, Messages.COMMAND_TEMPORARY_PERMISSIONS_SET,
                    Joiner.on(' ').join(temporaryPermissions));
        } else if (action.equalsIgnoreCase("cost")) {
            commands.setCost(args.getDouble(2));
            Messaging.sendTr(sender, Messages.COMMAND_COST_SET, args.getDouble(2));
        } else if (action.equalsIgnoreCase("expcost")) {
            commands.setExperienceCost((float) args.getDouble(2));
            Messaging.sendTr(sender, Messages.COMMAND_EXPERIENCE_COST_SET, args.getDouble(2));
        } else if (action.equalsIgnoreCase("hideerrors")) {
            commands.setHideErrorMessages(!commands.isHideErrorMessages());
            Messaging.sendTr(sender, commands.isHideErrorMessages() ? Messages.COMMAND_HIDE_ERROR_MESSAGES_SET
                    : Messages.COMMAND_HIDE_ERROR_MESSAGES_UNSET);
        } else if (action.equalsIgnoreCase("random")) {
            commands.setExecutionMode(
                    commands.getExecutionMode() == ExecutionMode.RANDOM ? ExecutionMode.LINEAR : ExecutionMode.RANDOM);
            Messaging.sendTr(sender, commands.getExecutionMode() == ExecutionMode.RANDOM ? Messages.COMMANDS_RANDOM_SET
                    : Messages.COMMANDS_RANDOM_UNSET);
        } else if (action.equalsIgnoreCase("itemcost")) {
            if (!(sender instanceof Player))
                throw new CommandException(CommandMessages.MUST_BE_INGAME);
            InventoryMenu.createSelfRegistered(new ItemRequirementGUI(commands)).present(((Player) sender));
        } else if (action.equalsIgnoreCase("errormsg")) {
            CommandTraitError which = Util.matchEnum(CommandTraitError.values(), args.getString(2));
            if (which == null)
                throw new CommandException(Messages.NPC_COMMAND_INVALID_ERROR_MESSAGE,
                        Util.listValuesPretty(CommandTraitError.values()));
            commands.setCustomErrorMessage(which, args.getString(3));
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "controllable|control (-m(ount),-y,-n,-o(wner required))",
            desc = "Toggles whether the NPC can be ridden and controlled",
            modifiers = { "controllable", "control" },
            min = 1,
            max = 1,
            flags = "myno")
    public void controllable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if ((npc.isSpawned() && !sender.hasPermission(
                "citizens.npc.controllable." + npc.getEntity().getType().name().toLowerCase().replace("_", "")))
                || !sender.hasPermission("citizens.npc.controllable"))
            throw new NoPermissionsException();
        if (!npc.hasTrait(Controllable.class)) {
            npc.addTrait(new Controllable(false));
        }

        Controllable trait = npc.getOrAddTrait(Controllable.class);
        boolean enabled = trait.toggle();
        if (args.hasFlag('y')) {
            enabled = trait.setEnabled(true);
        } else if (args.hasFlag('n')) {
            enabled = trait.setEnabled(false);
        }

        trait.setOwnerRequired(args.hasFlag('o'));
        String key = enabled ? Messages.CONTROLLABLE_SET : Messages.CONTROLLABLE_REMOVED;
        Messaging.sendTr(sender, key, npc.getName());
        if (enabled && args.hasFlag('m') && sender instanceof Player) {
            trait.mount((Player) sender);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "copy (--name newname)",
            desc = "Copies an NPC",
            modifiers = { "copy" },
            min = 1,
            max = 1,
            permission = "citizens.npc.copy")
    public void copy(CommandContext args, CommandSender sender, NPC npc, @Flag("name") String name)
            throws CommandException {
        if (name == null) {
            name = npc.getRawName();
        }
        NPC copy = npc.clone();
        if (!copy.getRawName().equals(name)) {
            copy.setName(name);
        }

        if (copy.getOrAddTrait(Spawned.class).shouldSpawn() && args.getSenderLocation() != null) {
            Location location = args.getSenderLocation();
            location.getChunk().load();
            copy.teleport(location, TeleportCause.COMMAND);
            copy.getOrAddTrait(CurrentLocation.class).setLocation(location);
        }

        CommandSenderCreateNPCEvent event = sender instanceof Player
                ? new PlayerCloneNPCEvent((Player) sender, npc, copy)
                : new CommandSenderCloneNPCEvent(sender, npc, copy);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            event.getNPC().destroy();
            String reason = "Couldn't create NPC.";
            if (!event.getCancelReason().isEmpty())
                reason += " Reason: " + event.getCancelReason();
            throw new CommandException(reason);
        }

        Messaging.sendTr(sender, Messages.NPC_COPIED, npc.getName());
        selector.select(sender, copy);
        history.add(sender, new CreateNPCHistoryItem(copy));
    }

    @Command(
            aliases = { "npc" },
            usage = "create [name] ((-b(aby),u(nspawned),s(ilent),t(emporary)) --at [x:y:z:world] --type [type] --item (item) --trait ['trait1, trait2...'] --registry [registry name])",
            desc = "Create a new NPC",
            flags = "bust",
            modifiers = { "create" },
            min = 2,
            permission = "citizens.npc.create")
    @Requirements
    public void create(CommandContext args, CommandSender sender, NPC npc, @Flag("at") Location at,
            @Flag(value = "type", defValue = "PLAYER") EntityType type, @Flag("trait") String traits,
            @Flag("item") String item, @Flag("template") String templateName, @Flag("registry") String registryName)
            throws CommandException {
        String name = args.getJoinedStrings(1).trim();
        if (args.hasValueFlag("type")) {
            if (type == null) {
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_INVALID_MOBTYPE, args.getFlag("type")));
            } else if (!EntityControllers.controllerExistsForType(type)) {
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_MISSING_MOBTYPE, args.getFlag("type")));
            }
        }

        int nameLength = SpigotUtil.getMaxNameLength(type);
        if (name.length() > nameLength && Placeholders.replace(name, sender, npc).length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG, nameLength);
            name = name.substring(0, nameLength);
        }

        if (name.length() == 0)
            throw new CommandException();

        if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall")
                && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", "")))
            throw new NoPermissionsException();

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        if (registryName != null) {
            registry = CitizensAPI.getNamedNPCRegistry(registryName);
            if (registry == null) {
                registry = CitizensAPI.createNamedNPCRegistry(name, new MemoryNPCDataStore());
                Messaging.send(sender, "An in-memory registry has been created named [[" + name + "]].");
            }
        }

        if (args.hasFlag('t')) {
            registry = temporaryRegistry;
        }

        if (item != null) {
            ItemStack stack = new ItemStack(Material.STONE, 1);
            try {
                Bukkit.getUnsafe().modifyItemStack(stack, item);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            npc = registry.createNPCUsingItem(type, name, stack);
        } else {
            npc = registry.createNPC(type, name);
        }

        String msg = "Created [[" + npc.getName() + "]] (ID [[" + npc.getId() + "]])";

        int age = 0;
        if (args.hasFlag('b')) {
            age = -24000;
            msg += " as a baby";
        }

        if (args.hasFlag('s')) {
            npc.data().set(NPC.SILENT_METADATA, true);
        }

        // Initialize necessary traits
        if (!Setting.SERVER_OWNS_NPCS.asBoolean()) {
            npc.getOrAddTrait(Owner.class).setOwner(sender);
        }

        npc.getOrAddTrait(MobType.class).setType(type);

        Location spawnLoc = null;
        if (sender instanceof Player) {
            spawnLoc = args.getSenderLocation();
        } else if (sender instanceof BlockCommandSender) {
            spawnLoc = args.getSenderLocation();
        }

        CommandSenderCreateNPCEvent event = sender instanceof Player ? new PlayerCreateNPCEvent((Player) sender, npc)
                : new CommandSenderCreateNPCEvent(sender, npc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            npc.destroy();
            String reason = "Couldn't create NPC.";
            if (!event.getCancelReason().isEmpty())
                reason += " Reason: " + event.getCancelReason();
            throw new CommandException(reason);
        }

        if (at != null) {
            if (!sender.hasPermission("citizens.npc.create-at-location"))
                throw new NoPermissionsException();
            spawnLoc = at;
            spawnLoc.getChunk().load();
        }

        if (spawnLoc == null) {
            npc.destroy();
            throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
        }

        if (!args.hasFlag('u')) {
            npc.spawn(spawnLoc, SpawnReason.CREATE);
        }

        if (traits != null) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(traits);
            StringBuilder builder = new StringBuilder();
            for (String tr : parts) {
                Trait trait = CitizensAPI.getTraitFactory().getTrait(tr);
                if (trait == null)
                    continue;
                npc.addTrait(trait);
                builder.append(StringHelper.wrap(tr) + ", ");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }

            msg += " with traits " + builder.toString();
        }

        if (templateName != null) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(templateName);
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                Template template = Template.byName(part);
                if (template == null)
                    continue;
                template.apply(npc);
                builder.append(StringHelper.wrap(part) + ", ");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }

            msg += " with templates " + builder.toString();
        }

        npc.getOrAddTrait(Age.class).setAge(age);

        selector.select(sender, npc);
        history.add(sender, new CreateNPCHistoryItem(npc));
        Messaging.send(sender, msg + '.');
    }

    @Command(
            aliases = { "npc" },
            usage = "debug -p(aths) -n(avigation)",
            desc = "Display debugging information",
            modifiers = { "debug" },
            min = 1,
            max = 1,
            flags = "pn",
            permission = "citizens.npc.debug")
    @Requirements(ownership = true, selected = true)
    public void debug(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.hasFlag('p')) {
            npc.getNavigator().getDefaultParameters().debug(!npc.getNavigator().getDefaultParameters().debug());
            Messaging.send(sender, "Path debugging set to " + npc.getNavigator().getDefaultParameters().debug());
        } else if (args.hasFlag('n')) {
            String output = "Use new finder [[" + npc.getNavigator().getDefaultParameters().useNewPathfinder();
            output += "]] distance margin [[" + npc.getNavigator().getDefaultParameters().distanceMargin()
                    + "]] (path margin [[" + npc.getNavigator().getDefaultParameters().pathDistanceMargin() + "]])<br>";
            output += "Teleport if below " + npc.getNavigator().getDefaultParameters().destinationTeleportMargin()
                    + " blocks<br>";
            output += "Range [[" + npc.getNavigator().getDefaultParameters().range() + "]] speed [["
                    + npc.getNavigator().getDefaultParameters().speed() + "]]<br>";
            output += "Stuck action [[" + npc.getNavigator().getDefaultParameters().stuckAction() + "]]<br>";
            Messaging.send(sender, output);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "despawn (id)",
            desc = "Despawn a NPC",
            modifiers = { "despawn" },
            min = 1,
            max = 2,
            permission = "citizens.npc.despawn")
    @Requirements
    public void despawn(final CommandContext args, final CommandSender sender, NPC npc) throws CommandException {
        NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
            @Override
            public void run(NPC npc) throws CommandException {
                if (npc == null) {
                    throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getString(1));
                }
                npc.getOrAddTrait(Spawned.class).setSpawned(false);
                npc.despawn(DespawnReason.REMOVAL);
                Messaging.sendTr(sender, Messages.NPC_DESPAWNED, npc.getName());
            }
        };
        if (npc == null || args.argsLength() == 2) {
            if (args.argsLength() < 2) {
                throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
            }
            NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                    args.getString(1));
        } else {
            callback.run(npc);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "drops",
            desc = "Edit an NPC's drops",
            modifiers = { "drops" },
            min = 1,
            max = 1,
            permission = "citizens.npc.drops")
    @Requirements(ownership = true, selected = true)
    public void drops(CommandContext args, Player sender, NPC npc) throws CommandException {
        DropsTrait trait = npc.getOrAddTrait(DropsTrait.class);
        trait.displayEditor(sender);
    }

    @Command(
            aliases = { "npc" },
            usage = "endercrystal -b(ottom)",
            desc = "Edit endercrystal modifiers",
            modifiers = { "endercrystal" },
            min = 1,
            max = 1,
            flags = "b",
            permission = "citizens.npc.endercrystal")
    @Requirements(ownership = true, selected = true, types = EntityType.ENDER_CRYSTAL)
    public void endercrystal(CommandContext args, Player sender, NPC npc) throws CommandException {
        if (args.hasFlag('b')) {
            EnderCrystalTrait trait = npc.getOrAddTrait(EnderCrystalTrait.class);
            boolean showing = !trait.isShowBase();
            trait.setShowBase(showing);
            Messaging.sendTr(sender,
                    showing ? Messages.ENDERCRYSTAL_SHOWING_BOTTOM : Messages.ENDERCRYSTAL_NOT_SHOWING_BOTTOM,
                    npc.getName());
            return;
        }
        throw new CommandException();
    }

    @Command(
            aliases = { "npc" },
            usage = "enderman -a(ngry)",
            desc = "Set enderman modifiers",
            flags = "a",
            modifiers = { "enderman" },
            min = 1,
            max = 2,
            permission = "citizens.npc.enderman")
    public void enderman(CommandContext args, Player sender, NPC npc) throws CommandException {
        if (args.hasFlag('a')) {
            boolean angry = npc.getOrAddTrait(EndermanTrait.class).toggleAngry();
            Messaging.sendTr(sender, angry ? Messages.ENDERMAN_ANGRY_SET : Messages.ENDERMAN_ANGRY_UNSET,
                    npc.getName());
        }
        throw new CommandUsageException();
    }

    @Command(
            aliases = { "npc" },
            usage = "flyable (true|false)",
            desc = "Toggles or sets an NPC's flyable status",
            modifiers = { "flyable" },
            min = 1,
            max = 2,
            permission = "citizens.npc.flyable")
    @Requirements(
            selected = true,
            ownership = true,
            excludedTypes = { EntityType.BAT, EntityType.BLAZE, EntityType.ENDER_DRAGON, EntityType.GHAST,
                    EntityType.WITHER })
    public void flyable(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Boolean explicit)
            throws CommandException {
        boolean flyable = explicit != null ? explicit : !npc.isFlyable();
        npc.setFlyable(flyable);
        flyable = npc.isFlyable(); // may not have applied, eg bats are always flyable
        Messaging.sendTr(sender, flyable ? Messages.FLYABLE_SET : Messages.FLYABLE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "follow (player name|NPC id) (-p[rotect])",
            desc = "Toggles NPC following you",
            flags = "p",
            modifiers = { "follow" },
            min = 1,
            max = 2,
            permission = "citizens.npc.follow")
    public void follow(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean protect = args.hasFlag('p');
        String name = sender.getName();
        if (args.argsLength() > 1) {
            name = args.getString(1);
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
                @Override
                public void run(NPC followingNPC) throws CommandException {
                    if (followingNPC == null)
                        throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
                    if (!(sender instanceof ConsoleCommandSender)
                            && !followingNPC.getOrAddTrait(Owner.class).isOwnedBy(sender))
                        throw new CommandException(CommandMessages.MUST_BE_OWNER);
                    if (followingNPC.getEntity() instanceof Player) {
                        boolean following = followingNPC.getOrAddTrait(FollowTrait.class)
                                .toggle((Player) followingNPC.getEntity(), protect);
                        Messaging.sendTr(sender, following ? Messages.FOLLOW_SET : Messages.FOLLOW_UNSET, npc.getName(),
                                followingNPC.getName());
                    } else {
                        throw new CommandException();
                    }
                }
            };
            NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                    args.getString(1));
            return;
        }
        boolean following = npc.getOrAddTrait(FollowTrait.class).toggle(player, protect);
        Messaging.sendTr(sender, following ? Messages.FOLLOW_SET : Messages.FOLLOW_UNSET, npc.getName(),
                player.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "gamemode [gamemode]",
            desc = "Changes the gamemode",
            modifiers = { "gamemode" },
            min = 1,
            max = 2,
            permission = "citizens.npc.gamemode")
    @Requirements(selected = true, ownership = true, types = { EntityType.PLAYER })
    public void gamemode(CommandContext args, CommandSender sender, NPC npc, @Arg(1) GameMode mode) {
        Player player = (Player) npc.getEntity();
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.GAMEMODE_DESCRIBE, npc.getName(),
                    player.getGameMode().name().toLowerCase());
            return;
        }
        if (mode == null) {
            Messaging.sendErrorTr(sender, Messages.GAMEMODE_INVALID, args.getString(1));
            return;
        }
        npc.getOrAddTrait(GameModeTrait.class).setGameMode(mode);
        Messaging.sendTr(sender, Messages.GAMEMODE_SET, Util.prettyEnum(mode));
    }

    @Command(
            aliases = { "npc" },
            usage = "glowing --color [minecraft chat color]",
            desc = "Toggles an NPC's glowing status",
            modifiers = { "glowing" },
            min = 1,
            max = 1,
            permission = "citizens.npc.glowing")
    @Requirements(selected = true, ownership = true)
    public void glowing(CommandContext args, CommandSender sender, NPC npc, @Flag("color") ChatColor color)
            throws CommandException {
        if (color != null) {
            npc.getOrAddTrait(ScoreboardTrait.class).setColor(color);
            if (!npc.data().has(NPC.Metadata.GLOWING)) {
                npc.data().setPersistent(NPC.Metadata.GLOWING, true);
            }
            Messaging.sendTr(sender, Messages.GLOWING_COLOR_SET, npc.getName(), color + Util.prettyEnum(color));
            return;
        }

        npc.data().setPersistent(NPC.Metadata.GLOWING, !npc.data().get(NPC.Metadata.GLOWING, false));
        boolean glowing = npc.data().get(NPC.Metadata.GLOWING);
        Messaging.sendTr(sender, glowing ? Messages.GLOWING_SET : Messages.GLOWING_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "gravity",
            desc = "Toggles gravity",
            modifiers = { "gravity" },
            min = 1,
            max = 1,
            permission = "citizens.npc.gravity")
    public void gravity(CommandContext args, CommandSender sender, NPC npc) {
        boolean nogravity = npc.getOrAddTrait(Gravity.class).toggle();
        String key = !nogravity ? Messages.GRAVITY_ENABLED : Messages.GRAVITY_DISABLED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "gui",
            desc = "Display NPC configuration GUI",
            modifiers = { "gui" },
            min = 1,
            max = 1,
            permission = "citizens.npc.gui")
    public void gui(CommandContext args, Player sender, NPC npc) {
        InventoryMenu.createSelfRegistered(new NPCConfigurator(npc)).present(sender);
    }

    @Command(
            aliases = { "npc" },
            usage = "hologram add [text] | set [line #] [text] | remove [line #] | clear | lineheight [height] | direction [up|down]",
            desc = "Controls NPC hologram text",
            modifiers = { "hologram" },
            min = 1,
            max = -1,
            permission = "citizens.npc.hologram")
    public void hologram(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "add", "set", "remove", "clear", "lineheight", "direction" }) String action)
            throws CommandException {
        HologramTrait trait = npc.getOrAddTrait(HologramTrait.class);
        if (args.argsLength() == 1) {
            String output = Messaging.tr(Messages.HOLOGRAM_DESCRIBE_HEADER, npc.getName());
            List<String> lines = trait.getLines();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                output += "<br>    [[" + i + "]] - " + line;
            }
            Messaging.send(sender, output);
            return;
        }

        if (action.equalsIgnoreCase("set")) {
            if (args.argsLength() == 2) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size()) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }

            if (args.argsLength() == 3) {
                throw new CommandException(Messages.HOLOGRAM_TEXT_MISSING);
            }

            trait.setLine(idx, args.getJoinedStrings(3));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_SET, idx, args.getJoinedStrings(3));
        } else if (action.equalsIgnoreCase("add")) {
            if (args.argsLength() == 2) {
                throw new CommandException(Messages.HOLOGRAM_TEXT_MISSING);
            }

            trait.addLine(args.getJoinedStrings(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_ADD, args.getJoinedStrings(2));
        } else if (action.equalsIgnoreCase("remove")) {
            if (args.argsLength() == 2) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size()) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }

            trait.removeLine(idx);
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_REMOVED, idx);
        } else if (action.equalsIgnoreCase("clear")) {
            trait.clear();
            Messaging.sendTr(sender, Messages.HOLOGRAM_CLEARED);
        } else if (action.equalsIgnoreCase("lineheight")) {
            trait.setLineHeight(args.getDouble(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_HEIGHT_SET, args.getDouble(2));
        } else if (action.equalsIgnoreCase("direction")) {
            HologramDirection direction = args.getString(2).equalsIgnoreCase("up") ? HologramDirection.BOTTOM_UP
                    : HologramDirection.TOP_DOWN;
            trait.setDirection(direction);
            Messaging.sendTr(sender, Messages.HOLOGRAM_DIRECTION_SET, Util.prettyEnum(direction));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "horse|donkey|mule (--color color) (--type type) (--style style) (-cb)",
            desc = "Sets horse and horse-like entity modifiers",
            help = "Use the -c flag to make the NPC have a chest, or the -b flag to stop them from having a chest.",
            modifiers = { "horse", "donkey", "mule" },
            min = 1,
            max = 1,
            flags = "cb",
            permission = "citizens.npc.horse")
    @Requirements(selected = true, ownership = true)
    public void horse(CommandContext args, CommandSender sender, NPC npc,
            @Flag({ "color", "colour" }) Horse.Color color, @Flag("style") Horse.Style style) throws CommandException {
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (!Util.isHorse(type)) {
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE, Util.prettyEnum(type));
        }
        HorseModifiers horse = npc.getOrAddTrait(HorseModifiers.class);
        String output = "";

        if (args.hasFlag('c')) {
            horse.setCarryingChest(true);
            output += Messaging.tr(Messages.HORSE_CHEST_SET) + " ";
        } else if (args.hasFlag('b')) {
            horse.setCarryingChest(false);
            output += Messaging.tr(Messages.HORSE_CHEST_UNSET) + " ";
        }

        if (type == EntityType.HORSE && (args.hasValueFlag("color") || args.hasValueFlag("colour"))) {
            if (color == null) {
                String valid = Util.listValuesPretty(Horse.Color.values());
                throw new CommandException(Messages.INVALID_HORSE_COLOR, valid);
            }
            horse.setColor(color);
            output += Messaging.tr(Messages.HORSE_COLOR_SET, Util.prettyEnum(color));
        }
        if (type == EntityType.HORSE && args.hasValueFlag("style")) {
            if (style == null) {
                String valid = Util.listValuesPretty(Horse.Style.values());
                throw new CommandException(Messages.INVALID_HORSE_STYLE, valid);
            }
            horse.setStyle(style);
            output += Messaging.tr(Messages.HORSE_STYLE_SET, Util.prettyEnum(style));
        }
        if (output.isEmpty()) {
            Messaging.sendTr(sender, Messages.HORSE_DESCRIBE, Util.prettyEnum(horse.getColor()), Util.prettyEnum(type),
                    Util.prettyEnum(horse.getStyle()));
        } else {
            Messaging.send(sender, output);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "hurt [damage]",
            desc = "Damages the NPC",
            modifiers = { "hurt" },
            min = 2,
            max = 2,
            permission = "citizens.npc.hurt")
    public void hurt(CommandContext args, CommandSender sender, NPC npc) {
        if (!(npc.getEntity() instanceof Damageable)) {
            Messaging.sendErrorTr(sender, Messages.NPC_NOT_DAMAGEABLE,
                    Util.prettyEnum(npc.getOrAddTrait(MobType.class).getType()));
            return;
        }
        if (npc.isProtected()) {
            Messaging.sendErrorTr(sender, Messages.NPC_PROTECTED);
            return;
        }
        ((Damageable) npc.getEntity()).damage(args.getInteger(1));
    }

    @Command(
            aliases = { "npc" },
            usage = "id",
            desc = "Sends the selected NPC's ID to the sender",
            modifiers = { "id" },
            min = 1,
            max = 1,
            permission = "citizens.npc.id")
    public void id(CommandContext args, CommandSender sender, NPC npc) {
        sender.sendMessage(Integer.toString(npc.getId()));
    }

    @Command(
            aliases = { "npc" },
            usage = "inventory",
            desc = "Show's an NPC's inventory",
            modifiers = { "inventory" },
            min = 1,
            max = 1,
            permission = "citizens.npc.inventory")
    public void inventory(CommandContext args, CommandSender sender, NPC npc) {
        npc.getOrAddTrait(Inventory.class).openInventory((Player) sender);
    }

    @Command(
            aliases = { "npc" },
            usage = "item (item) (metadata) (-h(and))",
            desc = "Sets the NPC's item",
            modifiers = { "item", },
            min = 1,
            max = 3,
            flags = "h",
            permission = "citizens.npc.item")
    @Requirements(selected = true, ownership = true)
    public void item(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Material mat, @Arg(2) String modify)
            throws CommandException {
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (!type.name().contains("ITEM_FRAME") && type != EntityType.DROPPED_ITEM && type != EntityType.FALLING_BLOCK)
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE);
        ItemStack stack = args.hasFlag('h') ? ((Player) sender).getItemInHand() : new ItemStack(mat, 1);
        if (modify != null) {
            try {
                Bukkit.getUnsafe().modifyItemStack(stack, modify);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (mat == null && !args.hasFlag('h'))
            throw new CommandException(Messages.UNKNOWN_MATERIAL);
        npc.setItemProvider(() -> stack);
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        }
        Messaging.sendTr(sender, Messages.ITEM_SET, Util.prettyEnum(stack.getType()));
    }

    @Command(
            aliases = { "npc" },
            usage = "jump",
            desc = "Makes the NPC jump",
            modifiers = { "jump" },
            min = 1,
            max = 1,
            permission = "citizens.npc.jump")
    public void jump(CommandContext args, CommandSender sender, NPC npc) {
        NMS.setShouldJump(npc.getEntity());
    }

    @Command(
            aliases = { "npc" },
            usage = "leashable",
            desc = "Toggles leashability",
            modifiers = { "leashable" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.leashable")
    public void leashable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.data().get(NPC.LEASH_PROTECTED_METADATA, true);
        if (args.hasFlag('t')) {
            npc.data().set(NPC.LEASH_PROTECTED_METADATA, vulnerable);
        } else {
            npc.data().setPersistent(NPC.LEASH_PROTECTED_METADATA, vulnerable);
        }
        String key = vulnerable ? Messages.LEASHABLE_STOPPED : Messages.LEASHABLE_SET;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "list (page) ((-a) --owner (owner) --type (type) --char (char) --registry (name))",
            desc = "List NPCs",
            flags = "a",
            modifiers = { "list" },
            min = 1,
            max = 2,
            permission = "citizens.npc.list")
    @Requirements
    public void list(CommandContext args, CommandSender sender, NPC npc, @Flag("owner") String owner,
            @Flag("type") EntityType type, @Flag("page") Integer page, @Flag("registry") String registry)
            throws CommandException {
        NPCRegistry source = registry != null ? CitizensAPI.getNamedNPCRegistry(registry)
                : CitizensAPI.getNPCRegistry();
        if (source == null)
            throw new CommandException();
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : source.sorted()) {
                npcs.add(add);
            }
        } else if (owner != null) {
            for (NPC add : source.sorted()) {
                if (!npcs.contains(add) && add.getOrAddTrait(Owner.class).isOwnedBy(owner)) {
                    npcs.add(add);
                }
            }
        } else if (sender instanceof Player) {
            for (NPC add : source.sorted()) {
                if (!npcs.contains(add) && add.getOrAddTrait(Owner.class).isOwnedBy(sender)) {
                    npcs.add(add);
                }
            }
        }

        if (args.hasValueFlag("type")) {
            if (type == null)
                throw new CommandException(Messages.COMMAND_INVALID_MOBTYPE, type);

            for (Iterator<NPC> iterator = npcs.iterator(); iterator.hasNext();) {
                if (iterator.next().getOrAddTrait(MobType.class).getType() != type) {
                    iterator.remove();
                }
            }
        }

        Paginator paginator = new Paginator().header("NPCs").console(sender instanceof ConsoleCommandSender)
                .enablePageSwitcher('/' + args.getRawCommand() + " --page $page");
        for (int i = 0; i < npcs.size(); i++) {
            int id = npcs.get(i).getId();
            String line = StringHelper.wrap(id) + " " + npcs.get(i).getName() + " (<click:run_command:/npc tp --id "
                    + id
                    + "><hover:show_text:Teleport to this NPC>[[tp]]</hover></click>) (<click:run_command:/npc tph --id "
                    + id
                    + "><hover:show_text:Teleport NPC to me>[[summon]]</hover></click>) (<click:run_command:/npc remove "
                    + id + "><hover:show_text:Remove this NPC><red>-</red></hover></click>)";
            paginator.addLine(line);
        }

        int op = page == null ? args.getInteger(1, 1) : page;
        if (!paginator.sendPage(sender, op)) {
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, op);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "lookclose --range [range] -r[ealistic looking] --randomlook [true|false] --randomswitchtargets [true|false] --randompitchrange [min,max] --randomyawrange [min,max] --disablewhennavigating [true|false]",
            desc = "Toggle whether a NPC will look when a player is near",
            modifiers = { "lookclose", "look" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "citizens.npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc,
            @Flag({ "randomlook", "rlook" }) Boolean randomlook, @Flag("range") Double range,
            @Flag("randomlookdelay") Integer randomLookDelay, @Flag("randomyawrange") String randomYaw,
            @Flag("randompitchrange") String randomPitch, @Flag("randomswitchtargets") Boolean randomSwitchTargets,
            @Flag("disablewhennavigating") Boolean disableWhenNavigating, @Flag("perplayer") Boolean perPlayer)
            throws CommandException {
        boolean toggle = true;
        LookClose trait = npc.getOrAddTrait(LookClose.class);
        if (randomlook != null) {
            trait.setRandomLook(randomlook);
            Messaging.sendTr(sender, randomlook ? Messages.LOOKCLOSE_RANDOM_SET : Messages.LOOKCLOSE_RANDOM_STOPPED,
                    npc.getName());
            toggle = false;
        }
        if (perPlayer != null) {
            trait.setPerPlayer(perPlayer);
            Messaging.sendTr(sender, perPlayer ? Messages.LOOKCLOSE_PERPLAYER_SET : Messages.LOOKCLOSE_PERPLAYER_UNSET,
                    npc.getName());
            toggle = false;
        }
        if (randomSwitchTargets != null) {
            trait.setRandomlySwitchTargets(randomSwitchTargets);
            Messaging.sendTr(sender, randomSwitchTargets ? Messages.LOOKCLOSE_RANDOM_TARGET_SWITCH_ENABLED
                    : Messages.LOOKCLOSE_RANDOM_TARGET_SWITCH_DISABLED, npc.getName());
            toggle = false;
        }
        if (disableWhenNavigating != null) {
            trait.setDisableWhileNavigating(disableWhenNavigating);
            Messaging.sendTr(sender, disableWhenNavigating ? Messages.LOOKCLOSE_DISABLE_WHEN_NAVIGATING
                    : Messages.LOOKCLOSE_ENABLE_WHEN_NAVIGATING, npc.getName());
            toggle = false;
        }
        if (range != null) {
            trait.setRange(range);
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANGE_SET, range);
            toggle = false;
        }
        if (args.hasFlag('r')) {
            trait.setRealisticLooking(!trait.useRealisticLooking());
            Messaging.sendTr(sender, trait.useRealisticLooking() ? Messages.LOOKCLOSE_REALISTIC_LOOK_SET
                    : Messages.LOOKCLOSE_REALISTIC_LOOK_UNSET, npc.getName());
            toggle = false;
        }
        if (randomLookDelay != null) {
            trait.setRandomLookDelay(Math.max(1, randomLookDelay));
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_DELAY_SET, npc.getName(), randomLookDelay);
            toggle = false;
        }
        if (randomPitch != null) {
            try {
                String[] parts = randomPitch.split(",");
                float min = Float.parseFloat(parts[0]), max = Float.parseFloat(parts[1]);
                if (min > max)
                    throw new IllegalArgumentException();
                trait.setRandomLookPitchRange(min, max);
            } catch (Exception e) {
                throw new CommandException(Messaging.tr(Messages.ERROR_SETTING_LOOKCLOSE_RANGE, randomPitch));
            }
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_PITCH_RANGE_SET, npc.getName(), randomPitch);
            toggle = false;
        }
        if (randomYaw != null) {
            try {
                String[] parts = randomYaw.split(",");
                float min = Float.parseFloat(parts[0]), max = Float.parseFloat(parts[1]);
                if (min > max)
                    throw new IllegalArgumentException();
                trait.setRandomLookYawRange(min, max);
            } catch (Exception e) {
                throw new CommandException(Messaging.tr(Messages.ERROR_SETTING_LOOKCLOSE_RANGE, randomYaw));
            }
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_YAW_RANGE_SET, npc.getName(), randomYaw);
            toggle = false;
        }
        if (toggle) {
            Messaging.sendTr(sender, trait.toggle() ? Messages.LOOKCLOSE_SET : Messages.LOOKCLOSE_STOPPED,
                    npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "metadata set|get|remove [key] (value) (-t(emporary))",
            desc = "Manages NPC metadata",
            modifiers = { "metadata" },
            flags = "t",
            min = 3,
            max = 4,
            permission = "citizens.npc.metadata")
    @Requirements(selected = true, ownership = true)
    public void metadata(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "set", "get", "remove" }) String command) throws CommandException {
        String key = args.getString(2);
        try {
            key = NPC.Metadata.valueOf(key.toUpperCase()).getKey();
        } catch (IllegalArgumentException e) {
        }

        if (command.equals("set")) {
            if (args.argsLength() != 4)
                throw new CommandException();

            Object metadata = args.getString(3);
            if (metadata.equals("false") || metadata.equals("true")) {
                metadata = Boolean.parseBoolean(args.getString(3));
            }
            try {
                metadata = Integer.parseInt(args.getString(3));
            } catch (NumberFormatException nfe) {
                try {
                    metadata = Double.parseDouble(args.getString(3));
                } catch (NumberFormatException nfe2) {
                }
            }

            if (args.hasFlag('t')) {
                npc.data().set(key, metadata);
            } else {
                npc.data().setPersistent(key, metadata);
            }
            Messaging.sendTr(sender, Messages.METADATA_SET, key, args.getString(3));
        } else if (command.equals("get")) {
            if (args.argsLength() != 3) {
                throw new CommandException();
            }
            sender.sendMessage(npc.data().get(key, "null"));
        } else if (command.equals("remove")) {
            if (args.argsLength() != 3) {
                throw new CommandException();
            }
            npc.data().remove(key);
            Messaging.sendTr(sender, Messages.METADATA_UNSET, key, npc.getName());
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "minecart (--item item_name(:data)) (--offset offset)",
            desc = "Sets minecart item",
            modifiers = { "minecart" },
            min = 1,
            max = 1,
            flags = "",
            permission = "citizens.npc.minecart")
    @Requirements(
            selected = true,
            ownership = true,
            types = { EntityType.MINECART, EntityType.MINECART_CHEST, EntityType.MINECART_COMMAND,
                    EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER, EntityType.MINECART_MOB_SPAWNER,
                    EntityType.MINECART_TNT })
    public void minecart(CommandContext args, CommandSender sender, NPC npc, @Flag("item") String item)
            throws CommandException {
        if (item != null) {
            int data = 0;
            if (item.contains(":")) {
                int dataIndex = item.indexOf(':');
                data = Integer.parseInt(item.substring(dataIndex + 1));
                item = item.substring(0, dataIndex);
            }
            Material material = Material.matchMaterial(item);
            if (material == null)
                throw new CommandException();
            npc.data().setPersistent(NPC.MINECART_ITEM_METADATA, material.name());
            npc.data().setPersistent(NPC.MINECART_ITEM_DATA_METADATA, data);
        }
        if (args.hasValueFlag("offset")) {
            npc.data().setPersistent(NPC.MINECART_OFFSET_METADATA, args.getFlagInteger("offset"));
        }

        Messaging.sendTr(sender, Messages.MINECART_SET, npc.data().get(NPC.MINECART_ITEM_METADATA, ""),
                npc.data().get(NPC.MINECART_ITEM_DATA_METADATA, 0), npc.data().get(NPC.MINECART_OFFSET_METADATA, 0));
    }

    @Command(
            aliases = { "npc" },
            usage = "mount (--onnpc <npc id>) (-c (ancel))",
            desc = "Mounts a controllable NPC",
            modifiers = { "mount" },
            min = 1,
            max = 1,
            flags = "c",
            permission = "citizens.npc.mount")
    public void mount(CommandContext args, CommandSender sender, NPC npc, @Flag("onnpc") String onnpc)
            throws CommandException {
        if (onnpc != null) {
            NPC mount;
            try {
                UUID uuid = UUID.fromString(onnpc);
                mount = CitizensAPI.getNPCRegistry().getByUniqueId(uuid);
            } catch (IllegalArgumentException ex) {
                mount = CitizensAPI.getNPCRegistry().getById(args.getFlagInteger("onnpc"));
            }
            if (mount == null || !mount.isSpawned()) {
                throw new CommandException(Messaging.tr(Messages.MOUNT_NPC_MUST_BE_SPAWNED, onnpc));
            }
            if (mount.equals(npc)) {
                throw new CommandException(Messages.TRIED_TO_MOUNT_NPC_ON_ITSELF);
            }
            NMS.mount(mount.getEntity(), npc.getEntity());
            return;
        } else if (args.hasFlag('c')) {
            npc.getOrAddTrait(MountTrait.class).unmount();
            return;
        }
        boolean enabled = npc.hasTrait(Controllable.class) && npc.getOrAddTrait(Controllable.class).isEnabled();
        if (!enabled) {
            Messaging.sendTr(sender, Messages.NPC_NOT_CONTROLLABLE, npc.getName());
            return;
        }
        if (!(sender instanceof Player)) {
            throw new CommandException(CommandMessages.MUST_BE_INGAME);
        }
        Player player = (Player) sender;
        boolean success = npc.getOrAddTrait(Controllable.class).mount(player);
        if (!success) {
            Messaging.sendTr(player, Messages.FAILED_TO_MOUNT_NPC, npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "moveto x:y:z:world | x y z world",
            desc = "Teleports a NPC to a given location",
            modifiers = "moveto",
            min = 1,
            valueFlags = { "x", "y", "z", "yaw", "pitch", "world" },
            permission = "citizens.npc.moveto")
    public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.isSpawned()) {
            npc.spawn(npc.getOrAddTrait(CurrentLocation.class).getLocation(), SpawnReason.COMMAND);
        }
        if (!npc.isSpawned()) {
            throw new CommandException("NPC could not be spawned.");
        }
        Location current = npc.getEntity().getLocation();
        Location to;
        if (args.argsLength() > 1) {
            String[] parts = Iterables.toArray(Splitter.on(':').split(args.getJoinedStrings(1, ':')), String.class);
            if (parts.length != 4 && parts.length != 3)
                throw new CommandException(Messages.MOVETO_FORMAT);
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = parts.length == 4 ? Bukkit.getWorld(parts[3]) : current.getWorld();
            if (world == null)
                throw new CommandException(Messages.WORLD_NOT_FOUND);
            to = new Location(world, x, y, z, current.getYaw(), current.getPitch());
        } else {
            to = current.clone();
            if (args.hasValueFlag("x"))
                to.setX(args.getFlagDouble("x"));
            if (args.hasValueFlag("y"))
                to.setY(args.getFlagDouble("y"));
            if (args.hasValueFlag("z"))
                to.setZ(args.getFlagDouble("z"));
            if (args.hasValueFlag("yaw"))
                to.setYaw((float) args.getFlagDouble("yaw"));
            if (args.hasValueFlag("pitch"))
                to.setPitch((float) args.getFlagDouble("pitch"));
            if (args.hasValueFlag("world")) {
                World world = Bukkit.getWorld(args.getFlag("world"));
                if (world == null)
                    throw new CommandException(Messages.WORLD_NOT_FOUND);
                to.setWorld(world);
            }
        }

        npc.teleport(to, TeleportCause.COMMAND);
        NMS.look(npc.getEntity(), to.getYaw(), to.getPitch());
        Messaging.sendTr(sender, Messages.MOVETO_TELEPORTED, npc.getName(), Util.prettyPrintLocation(to));
    }

    @Command(
            aliases = { "npc" },
            modifiers = { "name", "hidename" },
            usage = "name (-h(over))",
            desc = "Toggle nameplate visibility, or only show names on hover",
            min = 1,
            max = 1,
            flags = "h",
            permission = "citizens.npc.name")
    @Requirements(selected = true, ownership = true)
    public void name(CommandContext args, CommandSender sender, NPC npc) {
        String old = npc.data().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString();
        if (args.hasFlag('h')) {
            old = "hover";
        } else {
            old = old.equals("hover") ? "true" : "" + !Boolean.parseBoolean(old);
        }
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, old);
        Messaging.sendTr(sender, Messages.NAMEPLATE_VISIBILITY_SET, old);
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0, permission = "citizens.npc.info")
    public void npc(CommandContext args, CommandSender sender, final NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    ID: [[" + npc.getId());
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        Messaging.send(sender,
                "    UUID: [[" + npc.getUniqueId() + (npc.isSpawned() && type == EntityType.PLAYER ? "(v4)" : ""));
        Messaging.send(sender, "    Type: [[" + type);
        if (npc.isSpawned()) {
            Location loc = npc.getEntity().getLocation();
            String format = "    Spawned at [[%d, %d, %d]] in world [[%s";
            Messaging.send(sender,
                    String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
        }
        Messaging.send(sender, "    Traits");
        for (Trait trait : npc.getTraits()) {
            String message = "     [[- ]]" + trait.getName();
            Messaging.send(sender, message);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "ocelot (--type type) (-s(itting), -n(ot sitting))",
            desc = "Set the ocelot type of an NPC and whether it is sitting",
            modifiers = { "ocelot" },
            min = 1,
            max = 1,
            requiresFlags = true,
            flags = "sn",
            permission = "citizens.npc.ocelot")
    @Requirements(selected = true, ownership = true, types = { EntityType.OCELOT })
    public void ocelot(CommandContext args, CommandSender sender, NPC npc, @Flag("type") Ocelot.Type type)
            throws CommandException {
        OcelotModifiers trait = npc.getOrAddTrait(OcelotModifiers.class);
        if (args.hasFlag('s')) {
            trait.setSitting(true);
        } else if (args.hasFlag('n')) {
            trait.setSitting(false);
        }
        if (args.hasValueFlag("type")) {
            if (type == null) {
                String valid = Util.listValuesPretty(Ocelot.Type.values());
                throw new CommandException(Messages.INVALID_OCELOT_TYPE, valid);
            }
            trait.setType(type);
            if (!trait.supportsOcelotType()) {
                Messaging.sendErrorTr(sender, Messages.OCELOT_TYPE_DEPRECATED);
            }
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "owner [uuid|SERVER]",
            desc = "Set the owner of an NPC",
            modifiers = { "owner" },
            min = 1,
            max = 2,
            permission = "citizens.npc.owner")
    public void owner(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Owner ownerTrait = npc.getOrAddTrait(Owner.class);
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.NPC_OWNER, npc.getName(), ownerTrait.getOwnerId());
            return;
        }
        OfflinePlayer p;
        UUID uuid;
        if (args.getString(1).equalsIgnoreCase("SERVER")) {
            uuid = null;
        } else if ((p = Bukkit.getOfflinePlayer(args.getString(1))).hasPlayedBefore() || p.isOnline()) {
            uuid = p.getUniqueId();
        } else {
            uuid = UUID.fromString(args.getString(1));
        }
        if (ownerTrait.isOwnedBy(uuid))
            throw new CommandException(Messages.ALREADY_OWNER, uuid, npc.getName());
        ownerTrait.setOwner(uuid);
        boolean serverOwner = uuid == null;
        Messaging.sendTr(sender, serverOwner ? Messages.OWNER_SET_SERVER : Messages.OWNER_SET, npc.getName(), uuid);
    }

    @Command(
            aliases = { "npc" },
            usage = "passive (--set [true|false])",
            desc = "Sets whether an NPC damages other entities or not",
            modifiers = { "passive" },
            min = 1,
            max = 1,
            permission = "citizens.npc.passive")
    public void passive(CommandContext args, CommandSender sender, NPC npc, @Flag("set") Boolean set)
            throws CommandException {
        boolean passive = set != null ? set : !npc.data().get(NPC.DAMAGE_OTHERS_METADATA, true);
        npc.data().setPersistent(NPC.DAMAGE_OTHERS_METADATA, passive);
        Messaging.sendTr(sender, passive ? Messages.PASSIVE_SET : Messages.PASSIVE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "pathopt --avoid-water|aw [true|false] --stationary-ticks [ticks] --attack-range [range] --distance-margin [margin] --path-distance-margin [margin] --use-new-finder [true|false]",
            desc = "Sets an NPC's pathfinding options",
            modifiers = { "pathopt", "po", "patho" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pathfindingoptions")
    public void pathfindingOptions(CommandContext args, CommandSender sender, NPC npc,
            @Flag({ "avoid-water" }) Boolean avoidwater, @Flag("stationary-ticks") Integer stationaryTicks,
            @Flag("distance-margin") Double distanceMargin, @Flag("path-distance-margin") Double pathDistanceMargin,
            @Flag("attack-range") Double attackRange, @Flag("use-new-finder") Boolean useNewFinder)
            throws CommandException {
        String output = "";
        if (avoidwater != null) {
            npc.getNavigator().getDefaultParameters().avoidWater(avoidwater);
            output += Messaging.tr(avoidwater ? Messages.PATHFINDING_OPTIONS_AVOID_WATER_SET
                    : Messages.PATHFINDING_OPTIONS_AVOID_WATER_UNSET, npc.getName());
        }
        if (stationaryTicks != null) {
            if (stationaryTicks < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().stationaryTicks(stationaryTicks);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_STATIONARY_TICKS_SET, npc.getName(), stationaryTicks);
        }
        if (distanceMargin != null) {
            if (distanceMargin < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().distanceMargin(distanceMargin);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_DISTANCE_MARGIN_SET, npc.getName(), distanceMargin);

        }
        if (pathDistanceMargin != null) {
            if (pathDistanceMargin < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().pathDistanceMargin(pathDistanceMargin);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_PATH_DISTANCE_MARGIN_SET, npc.getName(),
                    pathDistanceMargin);
        }
        if (attackRange != null) {
            if (attackRange < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().attackRange(attackRange);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_ATTACK_RANGE_SET, npc.getName(), attackRange);
        }
        if (useNewFinder != null) {
            npc.getNavigator().getDefaultParameters().useNewPathfinder(useNewFinder);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_USE_NEW_FINDER, npc.getName(), useNewFinder);
        }
        if (output.isEmpty()) {
            throw new CommandUsageException();
        } else {
            Messaging.send(sender, output);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "pathrange [range]",
            desc = "Sets an NPC's pathfinding range",
            modifiers = { "pathrange", "pathfindingrange", "prange" },
            min = 2,
            max = 2,
            permission = "citizens.npc.pathfindingrange")
    public void pathfindingRange(CommandContext args, CommandSender sender, NPC npc) {
        double range = Math.max(1, args.getDouble(1));
        npc.getNavigator().getDefaultParameters().range((float) range);
        Messaging.sendTr(sender, Messages.PATHFINDING_RANGE_SET, range);
    }

    @Command(
            aliases = { "npc" },
            usage = "pathto me | here | cursor | [x] [y] [z]",
            desc = "Starts pathfinding to a certain location",
            modifiers = { "pathto" },
            min = 2,
            max = 4,
            permission = "citizens.npc.pathto")
    public void pathto(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "me", "here", "cursor" }) String option) throws CommandException {
        Location loc = npc.getStoredLocation();
        if (args.argsLength() == 2) {
            if ((option.equalsIgnoreCase("me") || option.equalsIgnoreCase("here"))) {
                loc = args.getSenderLocation();
            } else if (option.equalsIgnoreCase("cursor")) {
                loc = ((Player) sender).getTargetBlockExact(32).getLocation();
            } else {
                throw new CommandUsageException();
            }
        } else {
            loc.setX(args.getDouble(1));
            loc.setY(args.getDouble(2));
            loc.setZ(args.getDouble(3));
        }
        npc.getNavigator().setTarget(loc);
    }

    @Command(
            aliases = { "npc" },
            usage = "pickupitems (--set [true|false])",
            desc = "Allow NPC to pick up items",
            modifiers = { "pickupitems" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pickupitems")
    public void pickupitems(CommandContext args, CommandSender sender, NPC npc, @Flag("set") Boolean set)
            throws CommandException {
        boolean pickup = set == null ? !npc.data().get(NPC.Metadata.PICKUP_ITEMS, !npc.isProtected()) : set;
        npc.data().setPersistent(NPC.Metadata.PICKUP_ITEMS, pickup);
        Messaging.sendTr(sender, pickup ? Messages.PICKUP_ITEMS_SET : Messages.PICKUP_ITEMS_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "panimate [animation]",
            desc = "Plays a player animation",
            modifiers = { "panimate" },
            min = 2,
            max = 2,
            permission = "citizens.npc.panimate")
    @Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
    public void playerAnimate(CommandContext args, CommandSender sender, NPC npc, @Arg(1) PlayerAnimation animation)
            throws CommandException {
        if (animation == null) {
            Messaging.sendErrorTr(sender, Messages.UNKNOWN_PLAYER_ANIMATION,
                    Util.listValuesPretty(PlayerAnimation.values()));
            return;
        }
        animation.play((Player) npc.getEntity(), 64);
    }

    @Command(
            aliases = { "npc" },
            usage = "playerlist (-a,r)",
            desc = "Sets whether the NPC is put in the playerlist",
            modifiers = { "playerlist" },
            min = 1,
            max = 1,
            flags = "ar",
            permission = "citizens.npc.playerlist")
    @Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
    public void playerlist(CommandContext args, CommandSender sender, NPC npc) {
        boolean remove = !npc.data().get(NPC.REMOVE_FROM_PLAYERLIST_METADATA,
                Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
        if (args.hasFlag('a')) {
            remove = false;
        } else if (args.hasFlag('r')) {
            remove = true;
        }
        npc.data().setPersistent(NPC.REMOVE_FROM_PLAYERLIST_METADATA, remove);
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getOrAddTrait(CurrentLocation.class).getLocation(), SpawnReason.RESPAWN);
            NMS.addOrRemoveFromPlayerList(npc.getEntity(), remove);
        }
        Messaging.sendTr(sender, remove ? Messages.REMOVED_FROM_PLAYERLIST : Messages.ADDED_TO_PLAYERLIST,
                npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "pose (--save [name] (-d) | --mirror [name] (-d) | --assume [name] | --remove [name] | --default [name]) (-a)",
            desc = "Manage NPC poses",
            flags = "ad",
            modifiers = { "pose" },
            min = 1,
            max = 2,
            permission = "citizens.npc.pose")
    public void pose(CommandContext args, CommandSender sender, NPC npc, @Flag("save") String save,
            @Flag("mirror") String mirror, @Flag("assume") String assume, @Flag("remove") String remove,
            @Flag("default") String defaultPose) throws CommandException {
        Poses trait = npc.getOrAddTrait(Poses.class);
        if (save != null) {
            if (save.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(save, npc.getStoredLocation(), args.hasFlag('d'))) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else {
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, save);
            }
        } else if (mirror != null) {
            if (mirror.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(mirror, npc.getStoredLocation(), args.hasFlag('d'))) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else {
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, mirror);
            }
        } else if (defaultPose != null) {
            if (!trait.hasPose(defaultPose))
                throw new CommandException(Messages.POSE_MISSING, defaultPose);

            trait.setDefaultPose(defaultPose);
            Messaging.sendTr(sender, Messages.DEFAULT_POSE_SET, defaultPose);
        } else if (assume != null) {
            if (assume.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (!trait.hasPose(assume))
                throw new CommandException(Messages.POSE_MISSING, assume);

            trait.assumePose(assume);
        } else if (remove != null) {
            if (remove.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);
            if (trait.removePose(remove)) {
                Messaging.sendTr(sender, Messages.POSE_REMOVED);
            } else
                throw new CommandException(Messages.POSE_MISSING, remove);
        } else if (!args.hasFlag('a')) {
            trait.describe(sender, args.getInteger(1, 1));
        }

        if (!args.hasFlag('a'))
            return;
        if (args.getSenderLocation() == null)
            throw new ServerCommandException();
        trait.assumePose(args.getSenderLocation());
    }

    @Command(
            aliases = { "npc" },
            usage = "power",
            desc = "Toggle a creeper NPC as powered",
            modifiers = { "power" },
            min = 1,
            max = 1,
            permission = "citizens.npc.power")
    @Requirements(selected = true, ownership = true, types = { EntityType.CREEPER })
    public void power(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender,
                npc.getOrAddTrait(Powered.class).toggle() ? Messages.POWERED_SET : Messages.POWERED_STOPPED);
    }

    @Command(
            aliases = { "npc" },
            usage = "profession|prof [profession]",
            desc = "Set a NPC's profession",
            modifiers = { "profession", "prof" },
            min = 2,
            max = 2,
            permission = "citizens.npc.profession")
    @Requirements(selected = true, ownership = true)
    public void profession(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Profession parsed)
            throws CommandException {
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (type != EntityType.VILLAGER && !type.name().equals("ZOMBIE_VILLAGER")) {
            throw new RequirementMissingException(Messaging.tr(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE,
                    type.name().toLowerCase().replace('_', ' ')));
        }
        if (parsed == null) {
            throw new CommandException(Messages.INVALID_PROFESSION, args.getString(1),
                    Util.listValuesPretty(Profession.values()));
        }
        npc.getOrAddTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.sendTr(sender, Messages.PROFESSION_SET, npc.getName(), Util.prettyEnum(parsed));
    }

    @Command(
            aliases = { "npc" },
            usage = "rabbittype [type]",
            desc = "Set the Type of a Rabbit NPC",
            modifiers = { "rabbittype", "rbtype" },
            min = 2,
            permission = "citizens.npc.rabbittype")
    @Requirements(selected = true, ownership = true, types = { EntityType.RABBIT })
    public void rabbitType(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Rabbit.Type type)
            throws CommandException {
        if (type == null)
            throw new CommandException(Messages.INVALID_RABBIT_TYPE, Util.listValuesPretty(Rabbit.Type.values()));

        npc.getOrAddTrait(RabbitType.class).setType(type);
        Messaging.sendTr(sender, Messages.RABBIT_TYPE_SET, npc.getName(), type.name());
    }

    @Command(
            aliases = { "npc" },
            usage = "remove|rem (all|id|name| --owner [owner] | --eid [entity uuid] | --world [world])",
            desc = "Remove a NPC",
            modifiers = { "remove", "rem" },
            min = 1,
            max = 2)
    @Requirements
    public void remove(final CommandContext args, final CommandSender sender, NPC npc, @Flag("owner") String owner,
            @Flag("eid") UUID eid, @Flag("world") String world, @Arg(value = 1, completions = "all") String action)
            throws CommandException {
        if (owner != null) {
            Player playerOwner = Bukkit.getPlayerExact(owner);
            for (NPC rem : Lists.newArrayList(CitizensAPI.getNPCRegistry())) {
                if (!rem.getOrAddTrait(Owner.class).isOwnedBy(sender))
                    continue;
                if (playerOwner != null && rem.getOrAddTrait(Owner.class).isOwnedBy(playerOwner)) {
                    history.add(sender, new RemoveNPCHistoryItem(rem));
                    rem.destroy(sender);
                } else if (rem.getOrAddTrait(Owner.class).isOwnedBy(owner)) {
                    history.add(sender, new RemoveNPCHistoryItem(rem));
                    rem.destroy(sender);
                }
            }
            Messaging.sendTr(sender, Messages.NPCS_REMOVED);
            return;
        }

        if (world != null) {
            for (NPC rem : Lists.newArrayList(CitizensAPI.getNPCRegistry())) {
                Location loc = rem.getStoredLocation();
                if (loc != null && rem.getOrAddTrait(Owner.class).isOwnedBy(sender) && loc.getWorld() != null
                        && (loc.getWorld().getUID().toString().equals(world)
                                || loc.getWorld().getName().equalsIgnoreCase(world))) {
                    history.add(sender, new RemoveNPCHistoryItem(rem));
                    rem.destroy(sender);
                }
            }
            Messaging.sendTr(sender, Messages.NPCS_REMOVED);
            return;
        }

        if (eid != null) {
            Entity entity = Bukkit.getServer().getEntity(eid);
            if (entity != null && (npc = CitizensAPI.getNPCRegistry().getNPC(entity)) != null
                    && npc.getOrAddTrait(Owner.class).isOwnedBy(sender)) {
                history.add(sender, new RemoveNPCHistoryItem(npc));
                npc.destroy(sender);
                Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName(), npc.getId());
                return;
            } else {
                Messaging.sendErrorTr(sender, Messages.NPC_NOT_FOUND);
                return;
            }
        }
        if (args.argsLength() == 2) {
            if ("all".equalsIgnoreCase(action)) {
                if (!sender.hasPermission("citizens.admin.remove.all") && !sender.hasPermission("citizens.admin"))
                    throw new NoPermissionsException();
                for (NPC rem : CitizensAPI.getNPCRegistry()) {
                    history.add(sender, new RemoveNPCHistoryItem(rem));
                }
                CitizensAPI.getNPCRegistry().deregisterAll();
                Messaging.sendTr(sender, Messages.REMOVED_ALL_NPCS);
                return;
            } else {
                NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
                    @Override
                    public void run(NPC npc) throws CommandException {
                        if (npc == null)
                            throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
                        if (!(sender instanceof ConsoleCommandSender)
                                && !npc.getOrAddTrait(Owner.class).isOwnedBy(sender))
                            throw new CommandException(CommandMessages.MUST_BE_OWNER);
                        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
                            throw new NoPermissionsException();
                        history.add(sender, new RemoveNPCHistoryItem(npc));
                        npc.destroy(sender);
                        Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName(), npc.getId());
                    }
                };
                NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                        args.getString(1));
                return;
            }
        }
        if (npc == null)
            throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
        if (!(sender instanceof ConsoleCommandSender) && !npc.getOrAddTrait(Owner.class).isOwnedBy(sender))
            throw new CommandException(CommandMessages.MUST_BE_OWNER);
        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
            throw new NoPermissionsException();

        history.add(sender, new RemoveNPCHistoryItem(npc));
        npc.destroy(sender);
        Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName(), npc.getId());
    }

    @Command(
            aliases = { "npc" },
            usage = "rename [name]",
            desc = "Rename a NPC",
            modifiers = { "rename" },
            min = 2,
            permission = "citizens.npc.rename")
    public void rename(CommandContext args, CommandSender sender, NPC npc) {
        String oldName = npc.getName();
        String newName = args.getJoinedStrings(1);
        int nameLength = SpigotUtil.getMaxNameLength(npc.getOrAddTrait(MobType.class).getType());
        if (newName.length() > nameLength && Placeholders.replace(newName, sender, npc).length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG, nameLength);
            newName = newName.substring(0, nameLength);
        }
        npc.setName(newName);

        Messaging.sendTr(sender, Messages.NPC_RENAMED, oldName, newName);
    }

    @Command(
            aliases = { "npc" },
            usage = "respawn [delay]",
            desc = "Sets an NPC's respawn delay",
            modifiers = { "respawn" },
            min = 1,
            max = 2,
            permission = "citizens.npc.respawn")
    public void respawn(CommandContext args, CommandSender sender, NPC npc) {
        if (args.argsLength() > 1) {
            int delay = args.getTicks(1);
            npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, delay);
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_SET, delay);
        } else {
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_DESCRIBE, npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "rotate (--body [yaw]) (--head [yaw]) (--pitch [pitch]) (-s(mooth))",
            desc = "Rotate NPC",
            flags = "s",
            modifiers = { "rotate" },
            min = 1,
            max = 1,
            permission = "citizens.npc.rotate")
    public void rotate(CommandContext args, CommandSender sender, NPC npc, @Flag("body") Float yaw,
            @Flag("head") Float head, @Flag("pitch") Float pitch) {
        if (args.hasFlag('s')) {
            if (pitch == null) {
                pitch = npc.getStoredLocation().getPitch();
            }
            if (yaw == null) {
                if (head != null) {
                    yaw = head;
                } else {
                    yaw = NMS.getHeadYaw(npc.getEntity());
                }
            }
            npc.getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToHave(yaw, pitch);
            return;
        }
        if (yaw != null) {
            NMS.setBodyYaw(npc.getEntity(), yaw);
        }
        if (pitch != null) {
            NMS.setPitch(npc.getEntity(), pitch);
        }
        if (head != null) {
            NMS.setHeadYaw(npc.getEntity(), head);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "scoreboard --addtag [tags] --removetag [tags]",
            desc = "Controls an NPC's scoreboard",
            modifiers = { "scoreboard" },
            min = 1,
            max = 1,
            permission = "citizens.npc.scoreboard")
    public void scoreboard(CommandContext args, CommandSender sender, NPC npc, @Flag("addtag") String addTag,
            @Flag("removetag") String removeTag) {
        ScoreboardTrait trait = npc.getOrAddTrait(ScoreboardTrait.class);
        String output = "";
        if (addTag != null) {
            for (String tag : addTag.split(",")) {
                trait.addTag(tag);
            }
            output += " " + Messaging.tr(Messages.ADDED_SCOREBOARD_TAGS, addTag);
        }
        if (removeTag != null) {
            for (String tag : removeTag.split(",")) {
                trait.removeTag(tag);
            }
            output += " " + Messaging.tr(Messages.REMOVED_SCOREBOARD_TAGS, removeTag);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "script --add [files] --remove [files]",
            desc = "Controls an NPC's scripts",
            modifiers = { "script" },
            min = 1,
            max = 1,
            permission = "citizens.npc.script")
    public void script(CommandContext args, CommandSender sender, NPC npc, @Flag("add") String add,
            @Flag("remove") String remove) {
        ScriptTrait trait = npc.getOrAddTrait(ScriptTrait.class);
        if (add != null) {
            List<String> files = new ArrayList<String>();
            for (String file : add.split(",")) {
                if (!trait.validateFile(file)) {
                    Messaging.sendErrorTr(sender, Messages.INVALID_SCRIPT_FILE, file);
                    return;
                }
                files.add(file);
            }
            trait.addScripts(files);
        }
        if (remove != null) {
            trait.removeScripts(Arrays.asList(remove.split(",")));
        }
        Messaging.sendTr(sender, Messages.CURRENT_SCRIPTS, npc.getName(), Joiner.on("]],[[ ").join(trait.getScripts()));
    }

    @Command(
            aliases = { "npc" },
            usage = "select|sel [id|name] (--r range) (--registry [name])",
            desc = "Select a NPC with the given ID or name",
            modifiers = { "select", "sel" },
            min = 1,
            max = 2,
            permission = "citizens.npc.select")
    @Requirements
    public void select(CommandContext args, final CommandSender sender, final NPC npc,
            @Flag(value = "range", defValue = "10") double range, @Flag("registry") String registryName)
            throws CommandException {
        NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
            @Override
            public void run(NPC toSelect) throws CommandException {
                if (toSelect == null)
                    throw new CommandException(Messages.NPC_NOT_FOUND);
                if (npc != null && toSelect.getId() == npc.getId())
                    throw new CommandException(Messages.NPC_ALREADY_SELECTED);
                selector.select(sender, toSelect);
                Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
            }
        };
        NPCRegistry registry = registryName != null ? CitizensAPI.getNamedNPCRegistry(registryName)
                : CitizensAPI.getNPCRegistry();
        if (registry == null)
            throw new CommandException(Messages.UNKNOWN_NPC_REGISTRY, args.getFlag("registry"));
        if (args.argsLength() <= 1) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            Entity player = (Player) sender;
            final Location location = args.getSenderLocation();
            List<Entity> search = player.getNearbyEntities(range, range, range);
            Collections.sort(search, (o1, o2) -> {
                double d = o1.getLocation().distanceSquared(location) - o2.getLocation().distanceSquared(location);
                return d > 0 ? 1 : d < 0 ? -1 : 0;
            });
            for (Entity possibleNPC : search) {
                NPC test = registry.getNPC(possibleNPC);
                if (test == null)
                    continue;
                if (test.hasTrait(ClickRedirectTrait.class)) {
                    test = test.getTraitNullable(ClickRedirectTrait.class).getRedirectNPC();
                }
                callback.run(test);
                break;
            }
        } else {
            NPCCommandSelector.startWithCallback(callback, registry, sender, args, args.getString(1));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "sheep (--color [color]) (--sheared [sheared])",
            desc = "Sets sheep modifiers",
            modifiers = { "sheep" },
            min = 1,
            max = 1,
            permission = "citizens.npc.sheep")
    @Requirements(selected = true, ownership = true, types = { EntityType.SHEEP })
    public void sheep(CommandContext args, CommandSender sender, NPC npc, @Flag("color") DyeColor color,
            @Flag("sheared") Boolean sheared) throws CommandException {
        SheepTrait trait = npc.getOrAddTrait(SheepTrait.class);
        boolean hasArg = false;
        if (sheared != null) {
            trait.setSheared(sheared);
            hasArg = true;
        }
        if (args.hasValueFlag("color")) {
            if (color != null) {
                trait.setColor(color);
                Messaging.sendTr(sender, Messages.SHEEP_COLOR_SET, color.toString().toLowerCase());
            } else {
                Messaging.sendErrorTr(sender, Messages.INVALID_SHEEP_COLOR, Util.listValuesPretty(DyeColor.values()));
            }
            hasArg = true;
        }
        if (!hasArg) {
            throw new CommandException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "shop (edit|show|delete) (name)",
            desc = "NPC shop edit/show",
            modifiers = { "shop" },
            min = 1,
            max = 3,
            permission = "citizens.npc.shop")
    @Requirements(selected = false, ownership = true)
    public void shop(CommandContext args, Player sender, NPC npc,
            @Arg(value = 1, completions = { "edit", "show", "delete" }) String action) throws CommandException {
        if (args.argsLength() == 1) {
            if (npc != null) {
                npc.getOrAddTrait(ShopTrait.class).getDefaultShop().display(sender);
            }
            return;
        }
        NPCShop shop = npc != null ? npc.getOrAddTrait(ShopTrait.class).getDefaultShop() : null;
        if (args.argsLength() == 3) {
            shop = shops.getShop(args.getString(2).toLowerCase());
        }
        if (action.equalsIgnoreCase("delete")) {
            if (args.argsLength() != 3)
                throw new CommandUsageException();
            shops.deleteShop(args.getString(2).toLowerCase());
            return;
        }
        if (shop == null)
            throw new CommandUsageException();
        if (action.equalsIgnoreCase("edit")) {
            if (!sender.hasPermission("citizens.admin") && (!sender.hasPermission("citizens.npc.shop.edit")
                    || !sender.hasPermission("citizens.npc.shop.edit." + shop.getName())))
                throw new NoPermissionsException();
            shop.displayEditor(npc == null ? null : npc.getOrAddTrait(ShopTrait.class), sender);
        } else if (action.equalsIgnoreCase("show")) {
            shop.display(sender);
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "skin (-c(lear) -l(atest)) [name] (or --url [url] or -t [uuid/name] [data] [signature])",
            desc = "Sets an NPC's skin name. Use -l to set the skin to always update to the latest",
            modifiers = { "skin" },
            min = 1,
            max = 4,
            flags = "ctl",
            permission = "citizens.npc.skin")
    @Requirements(types = EntityType.PLAYER, selected = true, ownership = true)
    public void skin(final CommandContext args, final CommandSender sender, final NPC npc, @Flag("url") String url)
            throws CommandException {
        String skinName = npc.getName();
        final SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);
        if (args.hasFlag('c')) {
            trait.clearTexture();
        } else if (url != null) {
            Bukkit.getScheduler().runTaskAsynchronously(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    DataOutputStream out = null;
                    BufferedReader reader = null;
                    try {
                        URL target = new URL("https://api.mineskin.org/generate/url");
                        HttpURLConnection con = (HttpURLConnection) target.openConnection();
                        con.setRequestMethod("POST");
                        con.setDoOutput(true);
                        con.setConnectTimeout(1000);
                        con.setReadTimeout(30000);
                        out = new DataOutputStream(con.getOutputStream());
                        out.writeBytes("url=" + URLEncoder.encode(url, "UTF-8"));
                        out.close();
                        reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        JSONObject output = (JSONObject) new JSONParser().parse(reader);
                        JSONObject data = (JSONObject) output.get("data");
                        String uuid = (String) data.get("uuid");
                        JSONObject texture = (JSONObject) data.get("texture");
                        String textureEncoded = (String) texture.get("value");
                        String signature = (String) texture.get("signature");
                        con.disconnect();
                        Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    trait.setSkinPersistent(uuid, signature, textureEncoded);
                                    Messaging.sendTr(sender, Messages.SKIN_URL_SET, npc.getName(), url);
                                } catch (IllegalArgumentException e) {
                                    Messaging.sendErrorTr(sender, Messages.ERROR_SETTING_SKIN_URL, url);
                                }
                            }
                        });
                    } catch (Throwable t) {
                        if (Messaging.isDebugging()) {
                            t.printStackTrace();
                        }
                        Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                Messaging.sendErrorTr(sender, Messages.ERROR_SETTING_SKIN_URL, url);
                            }
                        });
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }

            });
            return;
        } else if (args.hasFlag('t')) {
            if (args.argsLength() != 4)
                throw new CommandException(Messages.SKIN_REQUIRED);
            trait.setSkinPersistent(args.getString(1), args.getString(3), args.getString(2));
            Messaging.sendTr(sender, Messages.SKIN_SET, npc.getName(), args.getString(1));
            return;
        } else {
            if (args.argsLength() != 2)
                throw new CommandException(Messages.SKIN_REQUIRED);
            if (args.hasFlag('l')) {
                trait.setShouldUpdateSkins(true);
            }
            skinName = args.getString(1);
        }
        Messaging.sendTr(sender, Messages.SKIN_SET, npc.getName(), skinName);
        trait.setSkinName(skinName, true);
    }

    @Command(
            aliases = { "npc" },
            usage = "skinlayers (--cape [true|false]) (--hat [true|false]) (--jacket [true|false]) (--sleeves [true|false]) (--pants [true|false])",
            desc = "Sets an NPC's skin layers visibility.",
            modifiers = { "skinlayers" },
            min = 1,
            max = 5,
            permission = "citizens.npc.skinlayers")
    @Requirements(types = EntityType.PLAYER, selected = true, ownership = true)
    public void skinLayers(final CommandContext args, final CommandSender sender, final NPC npc,
            @Flag("cape") Boolean cape, @Flag("hat") Boolean hat, @Flag("jacket") Boolean jacket,
            @Flag("sleeves") Boolean sleeves, @Flag("pants") Boolean pants) throws CommandException {
        SkinLayers trait = npc.getOrAddTrait(SkinLayers.class);
        if (cape != null) {
            trait.setVisible(Layer.CAPE, cape);
        }
        if (hat != null) {
            trait.setVisible(Layer.HAT, hat);
        }
        if (jacket != null) {
            trait.setVisible(Layer.JACKET, jacket);
        }
        if (sleeves != null) {
            trait.setVisible(Layer.LEFT_SLEEVE, sleeves);
            trait.setVisible(Layer.RIGHT_SLEEVE, sleeves);
        }
        if (pants != null) {
            trait.setVisible(Layer.LEFT_PANTS, pants);
            trait.setVisible(Layer.RIGHT_PANTS, pants);
        }
        Messaging.sendTr(sender, Messages.SKIN_LAYERS_SET, npc.getName(), trait.isVisible(Layer.CAPE),
                trait.isVisible(Layer.HAT), trait.isVisible(Layer.JACKET),
                trait.isVisible(Layer.LEFT_SLEEVE) || trait.isVisible(Layer.RIGHT_SLEEVE),
                trait.isVisible(Layer.LEFT_PANTS) || trait.isVisible(Layer.RIGHT_PANTS));
    }

    @Command(
            aliases = { "npc" },
            usage = "size [size]",
            desc = "Sets the NPC's size",
            modifiers = { "size" },
            min = 1,
            max = 2,
            permission = "citizens.npc.size")
    @Requirements(selected = true, ownership = true, types = { EntityType.MAGMA_CUBE, EntityType.SLIME })
    public void slimeSize(CommandContext args, CommandSender sender, NPC npc) {
        SlimeSize trait = npc.getOrAddTrait(SlimeSize.class);
        if (args.argsLength() <= 1) {
            trait.describe(sender);
            return;
        }
        int size = Math.max(-2, args.getInteger(1));
        trait.setSize(size);
        Messaging.sendTr(sender, Messages.SIZE_SET, npc.getName(), size);
    }

    @Command(
            aliases = { "npc" },
            usage = "sound (--death [death sound|d]) (--ambient [ambient sound|d]) (--hurt [hurt sound|d]) (-n(one)/-s(ilent)) (-d(efault))",
            desc = "Sets an NPC's played sounds",
            modifiers = { "sound" },
            flags = "dns",
            min = 1,
            max = 1,
            permission = "citizens.npc.sound")
    @Requirements(selected = true, ownership = true, livingEntity = true)
    public void sound(CommandContext args, CommandSender sender, NPC npc, @Flag("death") String death,
            @Flag("ambient") String ambient, @Flag("hurt") String hurt) throws CommandException {
        String ambientSound = npc.data().get(NPC.AMBIENT_SOUND_METADATA);
        String deathSound = npc.data().get(NPC.DEATH_SOUND_METADATA);
        String hurtSound = npc.data().get(NPC.HURT_SOUND_METADATA);
        if (args.getValueFlags().size() == 0 && args.getFlags().size() == 0) {
            Messaging.sendTr(sender, Messages.SOUND_INFO, npc.getName(), ambientSound, hurtSound, deathSound);
            return;
        }

        if (args.hasFlag('n')) {
            ambientSound = deathSound = hurtSound = "";
            npc.data().setPersistent(NPC.SILENT_METADATA, true);
        }
        if (args.hasFlag('s')) {
            npc.data().setPersistent(NPC.SILENT_METADATA, !npc.data().get(NPC.SILENT_METADATA, false));
        }
        if (args.hasFlag('d')) {
            ambientSound = deathSound = hurtSound = null;
            npc.data().setPersistent(NPC.SILENT_METADATA, false);
        } else {
            if (death != null) {
                deathSound = death.equals("d") ? null : NMS.getSound(death);
            }
            if (ambient != null) {
                ambientSound = ambient.equals("d") ? null : NMS.getSound(ambient);
            }
            if (hurt != null) {
                hurtSound = hurt.equals("d") ? null : NMS.getSound(hurt);
            }
        }
        if (deathSound == null) {
            npc.data().remove(NPC.DEATH_SOUND_METADATA);
        } else {
            npc.data().setPersistent(NPC.DEATH_SOUND_METADATA, deathSound);
        }
        if (hurtSound == null) {
            npc.data().remove(NPC.HURT_SOUND_METADATA);
        } else {
            npc.data().setPersistent(NPC.HURT_SOUND_METADATA, hurtSound);
        }
        if (ambientSound == null) {
            npc.data().remove(NPC.AMBIENT_SOUND_METADATA);
        } else {
            npc.data().setPersistent(NPC.AMBIENT_SOUND_METADATA, ambientSound);
        }

        if (ambientSound != null && ambientSound.isEmpty()) {
            ambientSound = "none";
        }
        if (hurtSound != null && hurtSound.isEmpty()) {
            hurtSound = "none";
        }
        if (deathSound != null && deathSound.isEmpty()) {
            deathSound = "none";
        }
        if ((!Strings.isNullOrEmpty(ambientSound) && !ambientSound.equals("none"))
                || (!Strings.isNullOrEmpty(deathSound) && !deathSound.equals("none"))
                || (!Strings.isNullOrEmpty(hurtSound) && !hurtSound.equals("none"))) {
            npc.data().setPersistent(NPC.SILENT_METADATA, false);
        }
        Messaging.sendTr(sender, Messages.SOUND_SET, npc.getName(), ambientSound, hurtSound, deathSound);
    }

    @Command(
            aliases = { "npc" },
            usage = "spawn (id|name) -l(oad chunks)",
            desc = "Spawn an existing NPC",
            modifiers = { "spawn" },
            min = 1,
            max = 2,
            flags = "l",
            permission = "citizens.npc.spawn")
    @Requirements(ownership = true)
    public void spawn(final CommandContext args, final CommandSender sender, NPC npc) throws CommandException {
        NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
            @Override
            public void run(NPC respawn) throws CommandException {
                if (respawn == null) {
                    if (args.argsLength() > 1) {
                        throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getString(1));
                    } else {
                        throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
                    }
                }
                if (respawn.isSpawned()) {
                    throw new CommandException(Messages.NPC_ALREADY_SPAWNED, respawn.getName());
                }
                Location location = respawn.getOrAddTrait(CurrentLocation.class).getLocation();
                if (location == null || args.hasValueFlag("location")) {
                    if (args.getSenderLocation() == null)
                        throw new CommandException(Messages.NO_STORED_SPAWN_LOCATION);

                    location = args.getSenderLocation();
                }
                if (args.hasFlag('l') && !Util.isLoaded(location)) {
                    location.getChunk().load();
                }
                if (respawn.spawn(location, SpawnReason.COMMAND)) {
                    selector.select(sender, respawn);
                    Messaging.sendTr(sender, Messages.NPC_SPAWNED, respawn.getName());
                }
            }
        };
        if (args.argsLength() > 1) {
            NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                    args.getString(1));
        } else {
            callback.run(npc);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "speak [message] --target [npcid|player name] --range (range to look for entities to speak to in blocks) (--type vocal_type)",
            desc = "Says a message from the NPC",
            modifiers = { "speak" },
            min = 2,
            permission = "citizens.npc.speak")
    public void speak(CommandContext args, CommandSender sender, NPC npc, @Flag("type") String type,
            @Flag("target") String target, @Flag("range") Float range) throws CommandException {
        String message = args.getJoinedStrings(1);

        if (message.length() <= 0) {
            Messaging.send(sender, "Default Vocal Chord for " + npc.getName() + ": "
                    + npc.getOrAddTrait(Speech.class).getDefaultVocalChord());
            return;
        }

        SpeechContext context = new SpeechContext(message);

        if (target != null) {
            if (target.matches("\\d+")) {
                NPC targetNPC = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(args.getFlag("target")));
                if (targetNPC != null)
                    context.addRecipient(targetNPC.getEntity());
            } else {
                Player player = Bukkit.getPlayerExact(target);
                if (player != null) {
                    context.addRecipient((Entity) player);
                }
            }
        }

        if (range != null) {
            npc.getEntity().getNearbyEntities(range / 2, range, range / 2).forEach((e) -> {
                if (!CitizensAPI.getNPCRegistry().isNPC(e)) {
                    context.addRecipient(e);
                }
            });
        }

        if (type == null || !CitizensAPI.getSpeechFactory().isRegistered(type)) {
            type = npc.getOrAddTrait(Speech.class).getDefaultVocalChord();
        }

        npc.getDefaultSpeechController().speak(context, type);
    }

    @Command(
            aliases = { "npc" },
            usage = "speed [speed]",
            desc = "Sets the movement speed of an NPC as a percentage",
            modifiers = { "speed" },
            min = 2,
            max = 2,
            permission = "citizens.npc.speed")
    public void speed(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        float newSpeed = (float) Math.abs(args.getDouble(1));
        if (newSpeed >= Setting.MAX_SPEED.asDouble())
            throw new CommandException(Messages.SPEED_MODIFIER_ABOVE_LIMIT);
        npc.getNavigator().getDefaultParameters().speedModifier(newSpeed);

        Messaging.sendTr(sender, Messages.SPEED_MODIFIER_SET, newSpeed);
    }

    @Command(
            aliases = { "npc" },
            usage = "swim (--set [true|false])",
            desc = "Sets an NPC to swim or not",
            modifiers = { "swim" },
            min = 1,
            max = 1,
            permission = "citizens.npc.swim")
    public void swim(CommandContext args, CommandSender sender, NPC npc, @Flag("set") Boolean set)
            throws CommandException {
        boolean swim = set != null ? set : !npc.data().get(NPC.Metadata.SWIMMING, true);
        npc.data().setPersistent(NPC.Metadata.SWIMMING, swim);
        Messaging.sendTr(sender, swim ? Messages.SWIMMING_SET : Messages.SWIMMING_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "target [name|UUID] (-a[ggressive]) (-c[ancel])",
            desc = "Target a given entity",
            modifiers = { "target" },
            flags = "ac",
            min = 1,
            max = 2,
            permission = "citizens.npc.target")
    public void target(CommandContext args, Player sender, NPC npc) {
        if (args.hasFlag('c')) {
            npc.getNavigator().cancelNavigation();
            return;
        }
        Entity toTarget = args.argsLength() < 2 ? sender : Bukkit.getPlayer(args.getString(1));
        if (toTarget == null) {
            toTarget = Bukkit.getEntity(UUID.fromString(args.getString(1)));
        }
        if (toTarget != null) {
            npc.getNavigator().setTarget(toTarget, args.hasFlag('a'));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "targetable (-t(emporary))",
            desc = "Toggles an NPC's targetability",
            modifiers = { "targetable" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.targetable")
    public void targetable(CommandContext args, CommandSender sender, NPC npc) {
        boolean targetable = !npc.data().get(NPC.TARGETABLE_METADATA, npc.isProtected());
        if (args.hasFlag('t')) {
            npc.data().set(NPC.TARGETABLE_METADATA, targetable);
        } else {
            npc.data().setPersistent(NPC.TARGETABLE_METADATA, targetable);
        }
        Messaging.sendTr(sender, targetable ? Messages.TARGETABLE_SET : Messages.TARGETABLE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "tp (-e(xact))",
            desc = "Teleport in front of an NPC",
            modifiers = { "tp", "teleport" },
            min = 1,
            max = 1,
            flags = "e",
            permission = "citizens.npc.tp")
    public void tp(CommandContext args, Player player, NPC npc) {
        Location to = npc.getOrAddTrait(CurrentLocation.class).getLocation();
        if (to == null) {
            Messaging.sendError(player, Messages.TELEPORT_NPC_LOCATION_NOT_FOUND);
            return;
        }
        if (!args.hasFlag('e')) {
            to = to.clone().add(to.getDirection().setY(0));
            to.setDirection(to.getDirection().multiply(-1)).setPitch(0);
        }
        player.teleport(to, TeleportCause.COMMAND);
        Messaging.sendTr(player, Messages.TELEPORTED_TO_NPC, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "tphere (cursor) (-c(enter))",
            desc = "Teleport a NPC to your location",
            flags = "c",
            modifiers = { "tphere", "tph", "move" },
            min = 1,
            max = 2,
            permission = "citizens.npc.tphere")
    public void tphere(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Location to = args.getSenderLocation();
        if (to == null)
            throw new ServerCommandException();
        if (args.argsLength() > 1 && args.getString(1).equalsIgnoreCase("cursor")) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            Block target = ((Player) sender).getTargetBlock(null, 64);
            if (target == null)
                throw new CommandException(Messages.MISSING_TP_CURSOR_BLOCK);
            to = target.getRelative(BlockFace.UP).getLocation();
        }
        if (!sender.hasPermission("citizens.npc.tphere.multiworld")
                && npc.getStoredLocation().getWorld() != args.getSenderLocation().getWorld()) {
            throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
        }
        if (args.hasFlag('c')) {
            to = to.getBlock().getLocation();
            to.setX(to.getX() + 0.5);
            to.setZ(to.getZ() + 0.5);
        }
        if (!npc.isSpawned()) {
            NPCTeleportEvent event = new NPCTeleportEvent(npc, to);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            npc.spawn(to, SpawnReason.COMMAND);
        } else {
            npc.teleport(to, TeleportCause.COMMAND);
        }
        Messaging.sendTr(sender, Messages.NPC_TELEPORTED, npc.getName(),
                Util.prettyPrintLocation(args.getSenderLocation()));
    }

    @Command(
            aliases = { "npc" },
            usage = "tpto [player name|npc id] [player name|npc id]",
            desc = "Teleport an NPC or player to another NPC or player",
            modifiers = { "tpto" },
            min = 2,
            max = 3,
            permission = "citizens.npc.tpto")
    @Requirements
    public void tpto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Entity from = null, to = null;
        boolean firstWasPlayer = false;
        if (npc != null) {
            from = npc.getEntity();
        }
        try {
            int id = args.getInteger(1);
            NPC fromNPC = CitizensAPI.getNPCRegistry().getById(id);
            if (fromNPC != null) {
                if (args.argsLength() == 2) {
                    to = fromNPC.getEntity();
                } else {
                    from = fromNPC.getEntity();
                }
            }
        } catch (NumberFormatException e) {
            if (args.argsLength() == 2) {
                to = Bukkit.getPlayerExact(args.getString(1));
            } else {
                from = Bukkit.getPlayerExact(args.getString(1));
            }
            firstWasPlayer = true;
        }
        if (args.argsLength() == 3) {
            try {
                int id = args.getInteger(2);
                NPC toNPC = CitizensAPI.getNPCRegistry().getById(id);
                if (toNPC != null) {
                    to = toNPC.getEntity();
                }
            } catch (NumberFormatException e) {
                if (!firstWasPlayer) {
                    to = Bukkit.getPlayerExact(args.getString(2));
                }
            }
        }
        if (from == null)
            throw new CommandException(Messages.FROM_ENTITY_NOT_FOUND);
        if (to == null)
            throw new CommandException(Messages.TO_ENTITY_NOT_FOUND);
        from.teleport(to);
        Messaging.sendTr(sender, Messages.TPTO_SUCCESS);
    }

    @Command(
            aliases = { "npc" },
            usage = "trackingrange [range]",
            desc = "Sets the tracking range",
            modifiers = { "trackingrange" },
            min = 1,
            max = 2,
            permission = "citizens.npc.trackingrange")
    public void trackingrange(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Integer range) {
        if (range == null) {
            npc.data().remove(NPC.Metadata.TRACKING_RANGE);
        } else {
            npc.data().setPersistent(NPC.Metadata.TRACKING_RANGE, range);
        }
        Messaging.sendTr(sender, Messages.TRACKING_RANGE_SET, range);
    }

    @Command(
            aliases = { "npc" },
            usage = "type [type]",
            desc = "Sets an NPC's entity type",
            modifiers = { "type" },
            min = 2,
            max = 2,
            permission = "citizens.npc.type")
    public void type(CommandContext args, CommandSender sender, NPC npc, @Arg(1) EntityType type)
            throws CommandException {
        if (type == null)
            throw new CommandException(Messages.INVALID_ENTITY_TYPE, args.getString(1));
        npc.setBukkitEntityType(type);
        Messaging.sendTr(sender, Messages.ENTITY_TYPE_SET, npc.getName(), args.getString(1));
    }

    @Command(
            aliases = { "npc" },
            usage = "undo (all)",
            desc = "Undoes the last action (currently only create/remove supported)",
            modifiers = { "undo" },
            min = 1,
            max = 2,
            permission = "citizens.npc.undo")
    @Requirements
    public void undo(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = "all") String action) throws CommandException {
        if ("all".equalsIgnoreCase(action)) {
            while (history.undo(sender)) {
            }
        } else if (history.undo(sender)) {
            Messaging.sendTr(sender, Messages.UNDO_SUCCESSFUL);
        } else {
            Messaging.sendTr(sender, Messages.UNDO_UNSUCCESSFUL);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "useitem (-o(ffhand))",
            desc = "Sets an NPC to  be using their held items",
            modifiers = { "useitem" },
            min = 1,
            max = 1,
            flags = "o",
            permission = "citizens.npc.useitem")
    public void useitem(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean offhand = args.hasFlag('o');
        if (offhand) {
            npc.data().setPersistent(NPC.Metadata.USING_OFFHAND_ITEM,
                    !npc.data().get(NPC.Metadata.USING_OFFHAND_ITEM, false));
            Messaging.sendTr(sender, Messages.TOGGLED_USING_OFFHAND_ITEM,
                    Boolean.toString(npc.data().get(NPC.Metadata.USING_OFFHAND_ITEM)));
        } else {
            npc.data().setPersistent(NPC.Metadata.USING_HELD_ITEM,
                    !npc.data().get(NPC.Metadata.USING_HELD_ITEM, false));
            Messaging.sendTr(sender, Messages.TOGGLED_USING_HELD_ITEM,
                    Boolean.toString(npc.data().get(NPC.Metadata.USING_HELD_ITEM)));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "vulnerable (-t(emporary))",
            desc = "Toggles an NPC's vulnerability",
            modifiers = { "vulnerable" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.vulnerable")
    public void vulnerable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.isProtected();
        if (args.hasFlag('t')) {
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        } else {
            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        }
        String key = vulnerable ? Messages.VULNERABLE_STOPPED : Messages.VULNERABLE_SET;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "wander",
            desc = "Sets the NPC to wander around",
            modifiers = { "wander" },
            min = 1,
            max = 1,
            permission = "citizens.npc.wander")
    public void wander(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Waypoints trait = npc.getOrAddTrait(Waypoints.class);
        trait.setWaypointProvider(trait.getCurrentProviderName().equals("wander") ? "linear" : "wander");
        Messaging.sendTr(sender, Messages.WAYPOINT_PROVIDER_SET, trait.getCurrentProviderName());
    }

    @Command(
            aliases = { "npc" },
            usage = "wither (--invulnerable [true|false]) (--arrow-shield [true|false])",
            desc = "Sets wither modifiers",
            modifiers = { "wither" },
            min = 1,
            requiresFlags = true,
            max = 1,
            permission = "citizens.npc.wither")
    @Requirements(selected = true, ownership = true, types = { EntityType.WITHER })
    public void wither(CommandContext args, CommandSender sender, NPC npc, @Flag("invulnerable") Boolean invulnerable,
            @Flag("arrow-shield") Boolean arrows) throws CommandException {
        WitherTrait trait = npc.getOrAddTrait(WitherTrait.class);
        if (invulnerable != null) {
            trait.setInvulnerable(invulnerable);
        }
        if (arrows != null) {
            trait.setBlocksArrows(arrows);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "wolf (-s(itting) a(ngry) t(amed) i(nfo)) --collar [hex rgb color|name]",
            desc = "Sets wolf modifiers",
            modifiers = { "wolf" },
            min = 1,
            max = 1,
            requiresFlags = true,
            flags = "sati",
            permission = "citizens.npc.wolf")
    @Requirements(selected = true, ownership = true, types = EntityType.WOLF)
    public void wolf(CommandContext args, CommandSender sender, NPC npc, @Flag("collar") String collar)
            throws CommandException {
        WolfModifiers trait = npc.getOrAddTrait(WolfModifiers.class);
        if (args.hasFlag('a')) {
            trait.setAngry(!trait.isAngry());
        }
        if (args.hasFlag('s')) {
            trait.setSitting(!trait.isSitting());
        }
        if (args.hasFlag('t')) {
            trait.setTamed(!trait.isTamed());
        }
        if (collar != null) {
            String unparsed = collar;
            DyeColor color = null;
            try {
                color = DyeColor.valueOf(unparsed.toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException e) {
                try {
                    int rgb = Integer.parseInt(unparsed.replace("#", ""), 16);
                    color = DyeColor.getByColor(org.bukkit.Color.fromRGB(rgb));
                } catch (NumberFormatException ex) {
                    throw new CommandException(Messages.COLLAR_COLOUR_NOT_RECOGNISED, unparsed);
                }
            }
            if (color == null)
                throw new CommandException(Messages.COLLAR_COLOUR_NOT_SUPPORTED, unparsed);
            trait.setCollarColor(color);
        }
        Messaging.sendTr(sender, Messages.WOLF_TRAIT_UPDATED, npc.getName(), trait.isAngry(), trait.isSitting(),
                trait.isTamed(), trait.getCollarColor().name());
    }
}
