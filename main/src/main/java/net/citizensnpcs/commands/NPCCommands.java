package net.citizensnpcs.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

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
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.commands.gui.NPCConfigurator;
import net.citizensnpcs.commands.history.CommandHistory;
import net.citizensnpcs.commands.history.CreateNPCHistoryItem;
import net.citizensnpcs.commands.history.RemoveNPCHistoryItem;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.BoundingBoxTrait;
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
import net.citizensnpcs.trait.HomeTrait;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.MirrorTrait;
import net.citizensnpcs.trait.MountTrait;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.PacketNPC;
import net.citizensnpcs.trait.PaintingTrait;
import net.citizensnpcs.trait.PausePathfindingTrait;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.ShopTrait;
import net.citizensnpcs.trait.ShopTrait.NPCShop;
import net.citizensnpcs.trait.SitTrait;
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
import net.citizensnpcs.util.MojangSkinGenerator;
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
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Ageable) && !(npc.getEntity() instanceof Zombie)
                && !npc.getEntity().getType().name().equals("TADPOLE"))
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
            usage = "aggressive [true|false]",
            desc = "Sets the aggressive status of the entity",
            modifiers = { "aggressive" },
            min = 1,
            max = 2,
            permission = "citizens.npc.aggressive")
    public void aggressive(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Boolean aggressive) {
        boolean aggro = aggressive != null ? aggressive : !npc.data().get(NPC.Metadata.AGGRESSIVE, false);
        npc.data().set(NPC.Metadata.AGGRESSIVE, aggro);
        NMS.setAggressive(npc.getEntity(), aggro);
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
            } else if (trait.addAnchor(save, args.getSenderLocation())) {
                Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
            } else
                throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, save);
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
            if (trait.removeAnchor(trait.getAnchor(remove))) {
                Messaging.sendTr(sender, Messages.ANCHOR_REMOVED);
            } else
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
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
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
            usage = "armorstand --visible [visible] --small [small] --marker [marker] --gravity [gravity] --arms [arms] --baseplate [baseplate] --(head|body|leftarm|leftleg|rightarm|rightleg)pose [angle x,y,z]",
            desc = "Edit armorstand properties",
            modifiers = { "armorstand" },
            min = 1,
            max = 1,
            valueFlags = { "bodypose", "leftarmpose", "rightarmpose", "leftlegpose", "rightlegpose" },
            permission = "citizens.npc.armorstand")
    @Requirements(selected = true, ownership = true, types = EntityType.ARMOR_STAND)
    public void armorstand(CommandContext args, CommandSender sender, NPC npc, @Flag("visible") Boolean visible,
            @Flag("small") Boolean small, @Flag("gravity") Boolean gravity, @Flag("arms") Boolean arms,
            @Flag("marker") Boolean marker, @Flag("baseplate") Boolean baseplate) throws CommandException {
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
        if (marker != null) {
            trait.setMarker(marker);
        }
        if (arms != null) {
            trait.setHasArms(arms);
        }
        if (baseplate != null) {
            trait.setHasBaseplate(baseplate);
        }
        ArmorStand ent = (ArmorStand) npc.getEntity();
        if (args.hasValueFlag("headpose")) {
            ent.setHeadPose(args.parseEulerAngle(args.getFlag("headpose")));
        }
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
        if (npc.getEntity() instanceof InventoryHolder) {
            cfg.blockBreaker((block, itemstack) -> {
                org.bukkit.inventory.Inventory inventory = ((InventoryHolder) npc.getEntity()).getInventory();
                Location location = npc.getEntity().getLocation();
                for (ItemStack drop : block.getDrops(itemstack)) {
                    for (ItemStack unadded : inventory.addItem(drop).values()) {
                        location.getWorld().dropItemNaturally(npc.getEntity().getLocation(), unadded);
                    }
                }
            });
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
        boolean enabled = !npc.data().get(NPC.Metadata.KEEP_CHUNK_LOADED, Setting.KEEP_CHUNKS_LOADED.asBoolean());
        if (args.hasFlag('t')) {
            npc.data().set(NPC.Metadata.KEEP_CHUNK_LOADED, enabled);
        } else {
            npc.data().setPersistent(NPC.Metadata.KEEP_CHUNK_LOADED, enabled);
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
        npc.data().setPersistent(NPC.Metadata.COLLIDABLE, !npc.data().get(NPC.Metadata.COLLIDABLE, !npc.isProtected()));
        Messaging.sendTr(sender,
                npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE) ? Messages.COLLIDABLE_SET : Messages.COLLIDABLE_UNSET,
                npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "command|cmd (add [command] | remove [id|all] | permissions [permissions] | sequential | cycle | random | clearerror [type] (name|uuid) | errormsg [type] [msg] | persistsequence [true|false] | cost [cost] (id) | expcost [cost] (id) | itemcost (id)) (-s(hift)) (-l[eft]/-r[ight]) (-p[layer] -o[p]), --cooldown --gcooldown [seconds] --delay [ticks] --permissions [perms] --n [max # of uses]",
            desc = "Controls commands which will be run when clicking on an NPC",
            help = Messages.NPC_COMMAND_HELP,
            modifiers = { "command", "cmd" },
            min = 1,
            flags = "lrpos",
            permission = "citizens.npc.command")
    public void command(CommandContext args, CommandSender sender, NPC npc,
            @Flag(value = { "permissions", "permission" }) String permissions,
            @Flag(value = "cooldown", defValue = "0") Duration cooldown,
            @Flag(value = "gcooldown", defValue = "0") Duration gcooldown, @Flag(value = "n", defValue = "-1") int n,
            @Flag(value = "delay", defValue = "0") Duration delay,
            @Arg(
                    value = 1,
                    completions = { "add", "remove", "permissions", "persistsequence", "sequential", "cycle", "random",
                            "hideerrors", "errormsg", "clearerror", "expcost", "itemcost", "cost" }) String action)
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
            if (command.toLowerCase().startsWith("npc select"))
                throw new CommandException("npc select not currently supported within commands. Use --id <id> instead");

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

            commands.clearHistory(which, args.argsLength() > 3 ? args.getString(3) : null);
            Messaging.send(sender, Messages.NPC_COMMAND_ERRORS_CLEARED, Util.prettyEnum(which));
        } else if (action.equalsIgnoreCase("sequential")) {
            commands.setExecutionMode(commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? ExecutionMode.LINEAR
                    : ExecutionMode.SEQUENTIAL);
            Messaging.sendTr(sender,
                    commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? Messages.COMMANDS_SEQUENTIAL_SET
                            : Messages.COMMANDS_SEQUENTIAL_UNSET);
        } else if (action.equalsIgnoreCase("cycle")) {
            commands.setExecutionMode(
                    commands.getExecutionMode() == ExecutionMode.CYCLE ? ExecutionMode.LINEAR : ExecutionMode.CYCLE);
            Messaging.sendTr(sender, commands.getExecutionMode() == ExecutionMode.CYCLE ? Messages.COMMANDS_CYCLE_SET
                    : Messages.COMMANDS_CYCLE_UNSET);
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
            if (args.getString(2).equalsIgnoreCase("all")) {
                commands.clear();
                Messaging.sendTr(sender, Messages.COMMANDS_CLEARED, npc.getName());
            } else {
                int id = args.getInteger(2, -1);
                if (!commands.hasCommandId(id))
                    throw new CommandException(Messages.COMMAND_UNKNOWN_COMMAND_ID, id);
                commands.removeCommandById(id);
                Messaging.sendTr(sender, Messages.COMMAND_REMOVED, id);
            }
        } else if (action.equalsIgnoreCase("permissions") || action.equalsIgnoreCase("perms")) {
            if (!sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            List<String> temporaryPermissions = Arrays.asList(args.getSlice(2));
            commands.setTemporaryPermissions(temporaryPermissions);
            Messaging.sendTr(sender, Messages.COMMAND_TEMPORARY_PERMISSIONS_SET,
                    Joiner.on(' ').join(temporaryPermissions));
        } else if (action.equalsIgnoreCase("cost")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.COMMAND_MISSING_COST);
            if (args.argsLength() == 4) {
                commands.setCost(args.getDouble(2), args.getInteger(3));
                Messaging.sendTr(sender, Messages.COMMAND_INDIVIDUAL_COST_SET,
                        args.getDouble(2) == -1 ? "-1 (default)" : args.getDouble(2), args.getInteger(3));
            } else {
                commands.setCost(args.getDouble(2));
                Messaging.sendTr(sender, Messages.COMMAND_COST_SET, args.getDouble(2));
            }
        } else if (action.equalsIgnoreCase("expcost")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.COMMAND_MISSING_COST);
            if (args.argsLength() == 4) {
                commands.setExperienceCost(args.getInteger(2), args.getInteger(3));
                Messaging.sendTr(sender, Messages.COMMAND_INDIVIDUAL_EXPERIENCE_COST_SET,
                        args.getInteger(2) == -1 ? "-1 (default)" : args.getInteger(2), args.getInteger(3));
            } else {
                commands.setExperienceCost(args.getInteger(2));
                Messaging.sendTr(sender, Messages.COMMAND_EXPERIENCE_COST_SET, args.getInteger(2));
            }
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
            if (args.argsLength() == 2) {
                InventoryMenu.createSelfRegistered(new ItemRequirementGUI(commands)).present((Player) sender);
            } else {
                InventoryMenu.createSelfRegistered(new ItemRequirementGUI(commands, args.getInteger(2)))
                        .present((Player) sender);
            }
        } else if (action.equalsIgnoreCase("errormsg")) {
            CommandTraitError which = Util.matchEnum(CommandTraitError.values(), args.getString(2));
            if (which == null)
                throw new CommandException(Messages.NPC_COMMAND_INVALID_ERROR_MESSAGE,
                        Util.listValuesPretty(CommandTraitError.values()));
            commands.setCustomErrorMessage(which, args.getString(3));
        } else
            throw new CommandUsageException();
    }

    @Command(
            aliases = { "npc" },
            usage = "configgui",
            desc = "Display NPC configuration GUI",
            modifiers = { "configgui" },
            min = 1,
            max = 1,
            permission = "citizens.npc.configgui")
    public void configgui(CommandContext args, Player sender, NPC npc) {
        InventoryMenu.createSelfRegistered(new NPCConfigurator(npc)).present(sender);
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
        if (npc.isSpawned()
                && !sender.hasPermission(
                        "citizens.npc.controllable." + npc.getEntity().getType().name().toLowerCase().replace("_", ""))
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
            if (!event.getCancelReason().isEmpty()) {
                reason += " Reason: " + event.getCancelReason();
            }
            throw new CommandException(reason);
        }
        Messaging.sendTr(sender, Messages.NPC_COPIED, npc.getName());
        selector.select(sender, copy);
        history.add(sender, new CreateNPCHistoryItem(copy));
    }

    @Command(
            aliases = { "npc" },
            usage = "create [name] ((-b(aby),u(nspawned),s(ilent),t(emporary),c(enter),p(acket)) --at [x,y,z,world] --type [type] --item (item) --trait ['trait1, trait2...'] --model [model name] --nameplate [true|false|hover] --temporaryticks [ticks] --registry [registry name]",
            desc = "Create a new NPC",
            flags = "bustpc",
            modifiers = { "create" },
            min = 2,
            permission = "citizens.npc.create")
    @Requirements
    public void create(CommandContext args, CommandSender sender, NPC npc, @Flag("at") Location at,
            @Flag(value = "type", defValue = "PLAYER") EntityType type, @Flag("trait") String traits,
            @Flag(value = "nameplate", completions = { "true", "false", "hover" }) String nameplate,
            @Flag("temporaryticks") Integer temporaryTicks, @Flag("item") String item,
            @Flag("template") String templateName, @Flag("registry") String registryName) throws CommandException {
        String name = args.getJoinedStrings(1).trim();
        if (args.hasValueFlag("type")) {
            if (type == null)
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_INVALID_MOBTYPE, args.getFlag("type")));
            else if (!EntityControllers.controllerExistsForType(type))
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_MISSING_MOBTYPE, args.getFlag("type")));
        }
        int nameLength = SpigotUtil.getMaxNameLength(type);
        if (Placeholders.replace(Messaging.parseComponents(name), sender, npc).length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG, nameLength);
            name = name.substring(0, nameLength);
        }
        if (name.length() == 0)
            throw new CommandException();

        if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall")
                && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", "")))
            throw new NoPermissionsException();

        if ((at != null || registryName != null || traits != null || templateName != null)
                && !sender.hasPermission("citizens.npc.admin"))
            throw new NoPermissionsException();

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        if (registryName != null) {
            registry = CitizensAPI.getNamedNPCRegistry(registryName);
            if (registry == null) {
                registry = CitizensAPI.createNamedNPCRegistry(registryName, new MemoryNPCDataStore());
                Messaging.send(sender, "An in-memory registry has been created named [[" + registryName + "]].");
            }
        }
        if (args.hasFlag('t') || temporaryTicks != null) {
            registry = temporaryRegistry;
        }
        if (item != null) {
            ItemStack stack = Util.parseItemStack(null, item);
            npc = registry.createNPCUsingItem(type, name, stack);
        } else {
            npc = registry.createNPC(type, name);
        }
        String msg = "Created [[" + npc.getName() + "]] (ID [[" + npc.getId() + "]])";

        if (args.hasFlag('b')) {
            msg += " as a baby";
            npc.getOrAddTrait(Age.class).setAge(-24000);
        }
        if (args.hasFlag('s')) {
            npc.data().set(NPC.Metadata.SILENT, true);
        }
        if (nameplate != null) {
            npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE,
                    nameplate.equalsIgnoreCase("hover") ? nameplate.toLowerCase() : Boolean.parseBoolean(nameplate));
        }
        if (!Setting.SERVER_OWNS_NPCS.asBoolean()) {
            npc.getOrAddTrait(Owner.class).setOwner(sender);
        }
        if (temporaryTicks != null) {
            NPC temp = npc;
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                if (temporaryRegistry.getByUniqueId(temp.getUniqueId()) == temp) {
                    temp.destroy();
                }
            }, temporaryTicks);
        }
        npc.getOrAddTrait(MobType.class).setType(type);

        if (args.hasFlag('p')) {
            npc.addTrait(PacketNPC.class);
        }
        Location spawnLoc = args.getSenderLocation();

        CommandSenderCreateNPCEvent event = sender instanceof Player ? new PlayerCreateNPCEvent((Player) sender, npc)
                : new CommandSenderCreateNPCEvent(sender, npc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            npc.destroy();
            String reason = "Couldn't create NPC.";
            if (!event.getCancelReason().isEmpty()) {
                reason += " Reason: " + event.getCancelReason();
            }
            throw new CommandException(reason);
        }
        if (at != null) {
            spawnLoc = at;
            spawnLoc.getChunk().load();
        }
        if (spawnLoc == null) {
            npc.destroy();
            throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
        }
        if (args.hasFlag('c')) {
            spawnLoc = Util.getCenterLocation(spawnLoc.getBlock());
        }
        if (!args.hasFlag('u')) {
            npc.spawn(spawnLoc, SpawnReason.CREATE);
        }
        if (traits != null) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(traits);
            StringBuilder builder = new StringBuilder();
            for (String tr : parts) {
                Trait trait = CitizensAPI.getTraitFactory().getTrait(tr);
                if (trait == null) {
                    continue;
                }
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
                if (template == null) {
                    continue;
                }
                template.apply(npc);
                builder.append(StringHelper.wrap(part) + ", ");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }
            msg += " with templates " + builder.toString();
        }
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
            usage = "deselect",
            desc = "Deselect currently selected NPC",
            modifiers = { "deselect", "desel" },
            min = 1,
            max = 1,
            permission = "citizens.npc.deselect")
    @Requirements
    public void deselect(CommandContext args, CommandSender sender, NPC npc) {
        selector.deselect(sender);
        Messaging.sendTr(sender, Messages.DESELECTED_NPC);
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
    public void despawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPCCommandSelector.Callback callback = npc1 -> {
            if (npc1 == null)
                throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getString(1));
            npc1.getOrAddTrait(Spawned.class).setSpawned(false);
            npc1.despawn(DespawnReason.REMOVAL);
            Messaging.sendTr(sender, Messages.NPC_DESPAWNED, npc1.getName());
        };
        if (npc == null || args.argsLength() == 2) {
            if (args.argsLength() < 2)
                throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
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
            usage = "follow (player name|NPC id) (-p[rotect]) (--margin [margin]) (--enable [boolean])",
            desc = "Toggles NPC following you",
            flags = "p",
            modifiers = { "follow" },
            min = 1,
            max = 2,
            permission = "citizens.npc.follow")
    public void follow(CommandContext args, CommandSender sender, NPC npc, @Flag("margin") Double margin,
            @Flag("enable") Boolean explicit) throws CommandException {
        boolean protect = args.hasFlag('p');
        FollowTrait trait = npc.getOrAddTrait(FollowTrait.class);
        if (margin != null) {
            trait.setFollowingMargin(margin);
            Messaging.sendTr(sender, Messages.FOLLOW_MARGIN_SET, npc.getName(), margin);
            return;
        }
        trait.setProtect(protect);
        String name = sender.getName();
        if (args.argsLength() > 1) {
            name = args.getString(1);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            NPCCommandSelector.Callback callback = followingNPC -> {
                if (followingNPC == null)
                    throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
                if (!(sender instanceof ConsoleCommandSender)
                        && !followingNPC.getOrAddTrait(Owner.class).isOwnedBy(sender))
                    throw new CommandException(CommandMessages.MUST_BE_OWNER);
                boolean following = !trait.isEnabled();
                trait.follow(following ? followingNPC.getEntity() : null);
                Messaging.sendTr(sender, following ? Messages.FOLLOW_SET : Messages.FOLLOW_UNSET, npc.getName(),
                        followingNPC.getName());
            };
            NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                    args.getString(1));
            return;
        }
        boolean following = explicit == null ? !trait.isEnabled() : explicit;
        trait.follow(following ? player.getPlayer() : null);
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
            usage = "hitbox --scale [scale] --width/height [value]",
            desc = "Sets the NPC hitbox",
            modifiers = { "hitbox" },
            min = 1,
            max = 1,
            permission = "citizens.npc.hitbox")
    public void hitbox(CommandContext args, CommandSender sender, NPC npc, @Flag("scale") Float scale,
            @Flag("width") Float width, @Flag("height") Float height) {
        if (scale != null) {
            npc.getOrAddTrait(BoundingBoxTrait.class).setScale(scale);
        }
        if (width != null) {
            npc.getOrAddTrait(BoundingBoxTrait.class).setWidth(width);
        }
        if (height != null) {
            npc.getOrAddTrait(BoundingBoxTrait.class).setHeight(height);
        }
        EntityDim dim = npc.getOrAddTrait(BoundingBoxTrait.class).getAdjustedBoundingBox();
        Messaging.sendTr(sender, Messages.BOUNDING_BOX_SET, "width " + dim.width + " height " + dim.height);
    }

    @Command(
            aliases = { "npc" },
            usage = "hologram add [text] | set [line #] [text] | remove [line #] | clear | lineheight [height] | viewrange [range] | margintop [line #] [margin] | marginbottom [line #] [margin]",
            desc = "Controls NPC hologram text",
            modifiers = { "hologram" },
            min = 1,
            max = -1,
            permission = "citizens.npc.hologram")
    public void hologram(CommandContext args, CommandSender sender, NPC npc,
            @Arg(
                    value = 1,
                    completions = { "add", "set", "remove", "clear", "lineheight", "viewrange", "margintop",
                            "marginbottom" }) String action,
            @Arg(value = 2, completionsProvider = HologramTrait.TabCompletions.class) String secondCompletion)
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
            if (args.argsLength() == 2)
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size())
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            if (args.argsLength() == 3)
                throw new CommandException(Messages.HOLOGRAM_TEXT_MISSING);

            trait.setLine(idx, args.getJoinedStrings(3));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_SET, idx, args.getJoinedStrings(3));
        } else if (action.equalsIgnoreCase("viewrange")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();

            trait.setViewRange(args.getInteger(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_VIEW_RANGE_SET, npc.getName(), args.getInteger(2));
        } else if (action.equalsIgnoreCase("add")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.HOLOGRAM_TEXT_MISSING);

            trait.addLine(args.getJoinedStrings(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_ADD, args.getJoinedStrings(2));
        } else if (action.equalsIgnoreCase("remove")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size())
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            trait.removeLine(idx);
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_REMOVED, idx);
        } else if (action.equalsIgnoreCase("clear")) {
            trait.clear();
            Messaging.sendTr(sender, Messages.HOLOGRAM_CLEARED);
        } else if (action.equalsIgnoreCase("lineheight")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();

            trait.setLineHeight(args.getDouble(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_HEIGHT_SET, args.getDouble(2));
        } else if (action.equalsIgnoreCase("margintop")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size())
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            if (args.argsLength() == 3)
                throw new CommandException(Messages.HOLOGRAM_MARGIN_MISSING);

            trait.setMargin(idx, "top", args.getDouble(3));
            Messaging.sendTr(sender, Messages.HOLOGRAM_MARGIN_SET, idx, "top", args.getDouble(3));
        } else if (action.equalsIgnoreCase("marginbottom")) {
            if (args.argsLength() == 2)
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            int idx = Math.max(0, args.getInteger(2));
            if (idx >= trait.getLines().size())
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);

            if (args.argsLength() == 3)
                throw new CommandException(Messages.HOLOGRAM_MARGIN_MISSING);

            trait.setMargin(idx, "bottom", args.getDouble(3));
            Messaging.sendTr(sender, Messages.HOLOGRAM_MARGIN_SET, idx, "bottom", args.getDouble(3));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "homeloc --location [loc] --delay [delay] --distance [distance] -h(ere) -p(athfind) -t(eleport)",
            desc = "Controls home location",
            modifiers = { "home" },
            min = 1,
            max = 1,
            flags = "pth",
            permission = "citizens.npc.home")
    @Requirements(ownership = true, selected = true)
    public void home(CommandContext args, CommandSender sender, NPC npc, @Flag("location") Location loc,
            @Flag("delay") Duration delay, @Flag("distance") Double distance) throws CommandException {
        HomeTrait trait = npc.getOrAddTrait(HomeTrait.class);
        String output = "";
        if (args.hasFlag('h')) {
            if (!(sender instanceof Player))
                throw new RequirementMissingException(Messaging.tr(CommandMessages.REQUIREMENTS_MUST_BE_LIVING_ENTITY));
            trait.setHomeLocation(((Player) sender).getLocation());
            output += Messaging.tr(Messages.HOME_TRAIT_LOCATION_SET, Util.prettyPrintLocation(trait.getHomeLocation()));
        }
        if (loc != null) {
            trait.setHomeLocation(loc);
            output += " "
                    + Messaging.tr(Messages.HOME_TRAIT_LOCATION_SET, Util.prettyPrintLocation(trait.getHomeLocation()));
        }
        if (distance != null) {
            trait.setDistanceBlocks(distance);
            output += " " + Messaging.tr(Messages.HOME_TRAIT_DISTANCE_SET, trait.getDistanceBlocks());
        }
        if (args.hasFlag('p')) {
            trait.setReturnStrategy(HomeTrait.ReturnStrategy.PATHFIND);
            output += " " + Messaging.tr(Messages.HOME_TRAIT_PATHFIND_SET, npc.getName());
        }
        if (args.hasFlag('t')) {
            trait.setReturnStrategy(HomeTrait.ReturnStrategy.TELEPORT);
            output += " " + Messaging.tr(Messages.HOME_TRAIT_TELEPORT_SET, npc.getName());
        }
        if (delay != null) {
            trait.setDelayTicks(Util.toTicks(delay));
            output += " " + Messaging.tr(Messages.HOME_TRAIT_DELAY_SET, Util.toTicks(delay));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
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
        if (!Util.isHorse(type))
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE, Util.prettyEnum(type));
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
        if (!type.name().contains("ITEM_FRAME") && !type.name().contains("ITEM_DISPLAY")
                && !type.name().contains("BLOCK_DISPLAY") && type != EntityType.DROPPED_ITEM
                && type != EntityType.FALLING_BLOCK)
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE, Util.prettyEnum(type));
        ItemStack stack = args.hasFlag('h') ? ((Player) sender).getItemInHand() : new ItemStack(mat, 1);
        if (modify != null) {
            stack = Util.parseItemStack(stack, modify);
        }
        if (mat == null && !args.hasFlag('h'))
            throw new CommandException(Messages.UNKNOWN_MATERIAL);
        ItemStack fstack = stack.clone();
        npc.setItemProvider(() -> fstack);

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
            usage = "knockback (--explicit true|false)",
            desc = "Toggle NPC knockback",
            modifiers = { "knockback" },
            min = 1,
            max = 1,
            permission = "citizens.npc.knockback")
    public void knockback(CommandContext args, CommandSender sender, NPC npc, @Flag("explicit") Boolean explicit) {
        boolean kb = !npc.data().get(NPC.Metadata.KNOCKBACK, true);
        if (explicit != null) {
            kb = explicit;
        }
        npc.data().set(NPC.Metadata.KNOCKBACK, kb);
        Messaging.sendTr(sender, kb ? Messages.KNOCKBACK_SET : Messages.KNOCKBACK_UNSET, npc.getName());
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
    @Requirements(selected = true, ownership = true, excludedTypes = { EntityType.PLAYER })
    public void leashable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.data().get(NPC.Metadata.LEASH_PROTECTED, true);
        if (args.hasFlag('t')) {
            npc.data().set(NPC.Metadata.LEASH_PROTECTED, vulnerable);
        } else {
            npc.data().setPersistent(NPC.Metadata.LEASH_PROTECTED, vulnerable);
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
        List<NPC> npcs = new ArrayList<>();

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
            String id = npcs.get(i).getUniqueId().toString();
            String line = StringHelper.wrap(npcs.get(i).getId()) + " " + npcs.get(i).getName()
                    + " (<click:run_command:/npc tp --uuid " + id
                    + "><hover:show_text:Teleport to this NPC>[[tp]]</hover></click>) (<click:run_command:/npc tph --uuid "
                    + id
                    + "><hover:show_text:Teleport NPC to me>[[summon]]</hover></click>) (<click:run_command:/npc remove "
                    + id + "><hover:show_text:Remove this NPC><red>-</red></hover></click>)";
            paginator.addLine(line);
        }
        int op = page == null ? args.getInteger(1, 1) : page;
        if (!paginator.sendPage(sender, op))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, op);
    }

    @Command(
            aliases = { "npc" },
            usage = "lookclose --range [range] -r[ealistic looking] --randomlook [true|false] --perplayer [true|false] --randomswitchtargets [true|false] --randompitchrange [min,max] --randomyawrange [min,max] --disablewhennavigating [true|false] --targetnpcs [true|false]",
            desc = "Toggle whether a NPC will look when a player is near",
            modifiers = { "lookclose", "look" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "citizens.npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc,
            @Flag({ "randomlook", "rlook" }) Boolean randomlook, @Flag("range") Double range,
            @Flag("randomlookdelay") Duration randomLookDelay, @Flag("randomyawrange") String randomYaw,
            @Flag("randompitchrange") String randomPitch, @Flag("randomswitchtargets") Boolean randomSwitchTargets,
            @Flag("headonly") Boolean headonly, @Flag("linkedbody") Boolean linkedbody,
            @Flag("disablewhennavigating") Boolean disableWhenNavigating, @Flag("perplayer") Boolean perPlayer,
            @Flag("targetnpcs") Boolean targetNPCs) throws CommandException {
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
        if (headonly != null) {
            trait.setHeadOnly(headonly);
            Messaging.sendTr(sender, headonly ? Messages.HEADONLY_SET : Messages.HEADONLY_UNSET, npc.getName());
            toggle = false;
        }
        if (linkedbody != null) {
            trait.setLinkedBody(linkedbody);
            Messaging.sendTr(sender, linkedbody ? Messages.LINKEDBODY_SET : Messages.LINKEDBODY_UNSET, npc.getName());
            toggle = false;
        }
        if (randomSwitchTargets != null) {
            trait.setRandomlySwitchTargets(randomSwitchTargets);
            Messaging.sendTr(sender, randomSwitchTargets ? Messages.LOOKCLOSE_RANDOM_TARGET_SWITCH_ENABLED
                    : Messages.LOOKCLOSE_RANDOM_TARGET_SWITCH_DISABLED, npc.getName());
            toggle = false;
        }
        if (targetNPCs != null) {
            trait.setTargetNPCs(targetNPCs);
            Messaging.sendTr(sender,
                    targetNPCs ? Messages.LOOKCLOSE_TARGET_NPCS_SET : Messages.LOOKCLOSE_TARGET_NPCS_UNSET,
                    npc.getName());
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
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANGE_SET, npc.getName(), range);
            toggle = false;
        }
        if (args.hasFlag('r')) {
            trait.setRealisticLooking(!trait.useRealisticLooking());
            Messaging.sendTr(sender, trait.useRealisticLooking() ? Messages.LOOKCLOSE_REALISTIC_LOOK_SET
                    : Messages.LOOKCLOSE_REALISTIC_LOOK_UNSET, npc.getName());
            toggle = false;
        }
        if (randomLookDelay != null) {
            trait.setRandomLookDelay(Math.max(1, Util.toTicks(randomLookDelay)));
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_DELAY_SET, npc.getName(), Util.toTicks(randomLookDelay));
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
            @Arg(value = 1, completions = { "set", "get", "remove" }) String command, @Arg(2) NPC.Metadata enumKey)
            throws CommandException {
        String key = args.getString(2);

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
                if (enumKey != null) {
                    npc.data().set(enumKey, metadata);
                } else {
                    npc.data().set(key, metadata);
                }
            } else if (enumKey != null) {
                npc.data().setPersistent(enumKey, metadata);
            } else {
                npc.data().setPersistent(key, metadata);
            }
            Messaging.sendTr(sender, Messages.METADATA_SET, enumKey != null ? enumKey : key, args.getString(3));
        } else if (command.equals("get")) {
            if (args.argsLength() != 3)
                throw new CommandException();
            Object data = enumKey != null ? npc.data().get(enumKey) : npc.data().get(key);
            if (data == null) {
                data = "null";
            }
            sender.sendMessage(data.toString());
        } else if (command.equals("remove")) {
            if (args.argsLength() != 3)
                throw new CommandException();
            if (enumKey != null) {
                npc.data().remove(enumKey);
            } else {
                npc.data().remove(key);
            }
            Messaging.sendTr(sender, Messages.METADATA_UNSET, enumKey != null ? enumKey : key, npc.getName());
        } else
            throw new CommandUsageException();
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
            npc.data().setPersistent(NPC.Metadata.MINECART_ITEM, material.name());
            npc.data().setPersistent(NPC.Metadata.MINECART_ITEM_DATA, data);
        }
        if (args.hasValueFlag("offset")) {
            npc.data().setPersistent(NPC.Metadata.MINECART_OFFSET, args.getFlagInteger("offset"));
        }
        Messaging.sendTr(sender, Messages.MINECART_SET, npc.data().get(NPC.Metadata.MINECART_ITEM, ""),
                npc.data().get(NPC.Metadata.MINECART_ITEM_DATA, 0), npc.data().get(NPC.Metadata.MINECART_OFFSET, 0));
    }

    @Command(
            aliases = { "npc" },
            modifiers = { "mirror" },
            usage = "mirror --name [true|false]",
            desc = "Controls mirroring of NPC skins and more",
            min = 1,
            max = 1,
            permission = "citizens.npc.mirror")
    @Requirements(selected = true, ownership = true)
    public void mirror(CommandContext args, CommandSender sender, NPC npc, @Flag("name") Boolean name)
            throws CommandException {
        if (((Citizens) CitizensAPI.getPlugin()).getProtocolLibListener() == null)
            throw new CommandException("ProtocolLib must be enabled to use this feature");

        MirrorTrait trait = npc.getOrAddTrait(MirrorTrait.class);
        if (name != null) {
            trait.setEnabled(true);
            trait.setMirrorName(name);
            Messaging.sendTr(sender, name ? Messages.MIRROR_NAME_SET : Messages.MIRROR_NAME_UNSET, npc.getName());
        } else {
            boolean enabled = !trait.isEnabled();
            trait.setEnabled(enabled);
            Messaging.sendTr(sender, enabled ? Messages.MIRROR_SET : Messages.MIRROR_UNSET, npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "mount (--onnpc <npc id|uuid>) (-c(ancel))",
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
            if (mount == null || !mount.isSpawned())
                throw new CommandException(Messaging.tr(Messages.MOUNT_NPC_MUST_BE_SPAWNED, onnpc));

            if (mount.equals(npc))
                throw new CommandException(Messages.TRIED_TO_MOUNT_NPC_ON_ITSELF);

            NMS.mount(mount.getEntity(), npc.getEntity());
            return;
        }
        if (args.hasFlag('c')) {
            npc.getOrAddTrait(MountTrait.class).unmount();
            return;
        }
        boolean enabled = npc.hasTrait(Controllable.class) && npc.getOrAddTrait(Controllable.class).isEnabled();
        if (!enabled) {
            Messaging.sendTr(sender, Messages.NPC_NOT_CONTROLLABLE, npc.getName());
            return;
        }
        if (!(sender instanceof Player))
            throw new CommandException(CommandMessages.MUST_BE_INGAME);

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
            if (!npc.isSpawned())
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
            if (args.hasValueFlag("x")) {
                to.setX(args.getFlagDouble("x"));
            }
            if (args.hasValueFlag("y")) {
                to.setY(args.getFlagDouble("y"));
            }
            if (args.hasValueFlag("z")) {
                to.setZ(args.getFlagDouble("z"));
            }
            if (args.hasValueFlag("yaw")) {
                to.setYaw((float) args.getFlagDouble("yaw"));
            }
            if (args.hasValueFlag("pitch")) {
                to.setPitch((float) args.getFlagDouble("pitch"));
            }
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
        npc.scheduleUpdate(NPCUpdate.PACKET);
        Messaging.sendTr(sender, Messages.NAMEPLATE_VISIBILITY_SET, old);
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0, permission = "citizens.npc.info")
    public void npc(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    ID: [[" + npc.getId());
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        Messaging.send(sender, "    UUID: [[" + npc.getUniqueId());
        Messaging.send(sender, "    Type: [[" + type);
        if (npc.isSpawned()) {
            Location loc = npc.getEntity().getLocation();
            String format = "    Spawned at [[%d, %d, %d, %.2f, %.2f (head %.2f)]] [[%s";
            Messaging.send(sender,
                    String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                            NMS.getYaw(npc.getEntity()), loc.getPitch(), NMS.getHeadYaw(npc.getEntity()),
                            loc.getWorld().getName()));
        }
        Messaging.send(sender, "    Traits");
        for (Trait trait : npc.getTraits()) {
            String message = "     - [[" + trait.getName();
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
            Messaging.sendTr(sender, Messages.NPC_OWNER, npc.getName(), ownerTrait.getOwner());
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
            usage = "packet --enabled [true|false]",
            desc = "Controls packet NPC settings",
            modifiers = { "packet" },
            min = 1,
            max = 1,
            permission = "citizens.npc.packet")
    @Requirements(selected = true, ownership = true)
    public void packet(CommandContext args, CommandSender sender, NPC npc, @Flag("enabled") Boolean explicit)
            throws CommandException {
        if (explicit == null) {
            explicit = !npc.hasTrait(PacketNPC.class);
        }
        if (explicit) {
            npc.getOrAddTrait(PacketNPC.class);
            Messaging.sendTr(sender, Messages.NPC_PACKET_ENABLED, npc.getName());
        } else {
            npc.removeTrait(PacketNPC.class);
            Messaging.sendTr(sender, Messages.NPC_PACKET_DISABLED, npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "painting (--art art)",
            desc = "Set painting modifiers",
            modifiers = { "painting" },
            min = 1,
            max = 1,
            permission = "citizens.npc.painting")
    @Requirements(selected = true, ownership = true, types = { EntityType.PAINTING })
    public void painting(CommandContext args, CommandSender sender, NPC npc, @Flag("art") Art art)
            throws CommandException {
        PaintingTrait trait = npc.getOrAddTrait(PaintingTrait.class);
        if (art != null) {
            trait.setArt(art);
            Messaging.sendTr(sender, Messages.PAINTING_ART_SET, npc.getName(), Util.prettyEnum(art));
            return;
        }
        throw new CommandUsageException();
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
        boolean passive = set != null ? set : !npc.data().get(NPC.Metadata.DAMAGE_OTHERS, true);
        npc.data().setPersistent(NPC.Metadata.DAMAGE_OTHERS, passive);
        Messaging.sendTr(sender, passive ? Messages.PASSIVE_SET : Messages.PASSIVE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "pathopt --avoid-water|aw [true|false] --stationary-ticks [ticks] --attack-range [range] --distance-margin [margin] --path-distance-margin [margin] --use-new-finder [true|false] --falling-distance [distance]",
            desc = "Sets an NPC's pathfinding options",
            modifiers = { "pathopt", "po", "patho" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pathfindingoptions")
    public void pathfindingOptions(CommandContext args, CommandSender sender, NPC npc,
            @Flag("avoid-water") Boolean avoidwater, @Flag("open-doors") Boolean opendoors,
            @Flag("stationary-ticks") Integer stationaryTicks, @Flag("distance-margin") Double distanceMargin,
            @Flag("path-distance-margin") Double pathDistanceMargin, @Flag("attack-range") Double attackRange,
            @Flag("use-new-finder") Boolean useNewFinder, @Flag("falling-distance") Integer fallingDistance)
            throws CommandException {
        String output = "";
        if (avoidwater != null) {
            npc.getNavigator().getDefaultParameters().avoidWater(avoidwater);
            output += Messaging.tr(avoidwater ? Messages.PATHFINDING_OPTIONS_AVOID_WATER_SET
                    : Messages.PATHFINDING_OPTIONS_AVOID_WATER_UNSET, npc.getName());
        }
        if (opendoors != null) {
            npc.data().setPersistent(NPC.Metadata.PATHFINDER_OPEN_DOORS, opendoors);
            output += Messaging.tr(opendoors ? Messages.PATHFINDING_OPTIONS_OPEN_DOORS_SET
                    : Messages.PATHFINDING_OPTIONS_OPEN_DOORS_UNSET, npc.getName());
        }
        if (stationaryTicks != null) {
            if (stationaryTicks < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().stationaryTicks(stationaryTicks);
            output += " "
                    + Messaging.tr(Messages.PATHFINDING_OPTIONS_STATIONARY_TICKS_SET, npc.getName(), stationaryTicks);
        }
        if (distanceMargin != null) {
            if (distanceMargin < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().distanceMargin(distanceMargin);
            output += " "
                    + Messaging.tr(Messages.PATHFINDING_OPTIONS_DISTANCE_MARGIN_SET, npc.getName(), distanceMargin);

        }
        if (pathDistanceMargin != null) {
            if (pathDistanceMargin < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().pathDistanceMargin(pathDistanceMargin);
            output += " " + Messaging.tr(Messages.PATHFINDING_OPTIONS_PATH_DISTANCE_MARGIN_SET, npc.getName(),
                    pathDistanceMargin);
        }
        if (attackRange != null) {
            if (attackRange < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().attackRange(attackRange);
            output += " " + Messaging.tr(Messages.PATHFINDING_OPTIONS_ATTACK_RANGE_SET, npc.getName(), attackRange);
        }
        if (useNewFinder != null) {
            npc.getNavigator().getDefaultParameters().useNewPathfinder(useNewFinder);
            output += " " + Messaging.tr(Messages.PATHFINDING_OPTIONS_USE_NEW_FINDER, npc.getName(), useNewFinder);
        }
        if (fallingDistance != null) {
            npc.data().set(NPC.Metadata.PATHFINDER_FALL_DISTANCE, fallingDistance);
            output += " "
                    + Messaging.tr(Messages.PATHFINDING_OPTIONS_FALLING_DISTANCE_SET, npc.getName(), fallingDistance);
        }
        if (output.isEmpty())
            throw new CommandUsageException();
        else {
            Messaging.send(sender, output.trim());
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
            usage = "pathto me | here | cursor | [x] [y] [z] (--margin [distance margin]) (-s[traight line])",
            desc = "Starts pathfinding to a certain location",
            modifiers = { "pathto" },
            min = 2,
            max = 4,
            flags = "s",
            permission = "citizens.npc.pathto")
    public void pathto(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "me", "here", "cursor" }) String option, @Flag("margin") Double margin)
            throws CommandException {
        Location loc = npc.getStoredLocation();
        if (args.argsLength() == 2) {
            if (option.equalsIgnoreCase("me") || option.equalsIgnoreCase("here")) {
                loc = args.getSenderLocation();
            } else if (option.equalsIgnoreCase("cursor")) {
                loc = ((Player) sender).getTargetBlockExact(32).getLocation();
            } else
                throw new CommandUsageException();
        } else {
            loc.setX(args.getDouble(1));
            loc.setY(args.getDouble(2));
            loc.setZ(args.getDouble(3));
        }
        if (args.hasFlag('s')) {
            npc.getNavigator().setStraightLineTarget(loc);
        } else {
            npc.getNavigator().setTarget(loc);
        }
        if (margin != null) {
            npc.getNavigator().getLocalParameters().distanceMargin(margin);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "pausepathfinding --onrightclick [true|false] --when-player-within [range in blocks] --pauseticks [ticks]",
            desc = "Sets pathfinding pause",
            modifiers = { "pausepathfinding" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pausepathfinding")
    public void pausepathfinding(CommandContext args, CommandSender sender, NPC npc,
            @Flag("onrightclick") Boolean rightclick, @Flag("when-player-within") Double playerRange,
            @Flag("pauseticks") Integer ticks) throws CommandException {
        PausePathfindingTrait trait = npc.getOrAddTrait(PausePathfindingTrait.class);
        if (playerRange != null) {
            if (playerRange <= 0)
                throw new CommandException("Invalid range");
            trait.setPlayerRangeBlocks(playerRange);
            Messaging.sendTr(sender, Messages.PAUSEPATHFINDING_RANGE_SET, npc.getName(), playerRange);
        }
        if (rightclick != null) {
            trait.setRightClick(rightclick);
            Messaging.sendTr(sender,
                    rightclick ? Messages.PAUSEPATHFINDING_RIGHTCLICK_SET : Messages.PAUSEPATHFINDING_RIGHTCLICK_UNSET,
                    npc.getName());
        }
        if (ticks != null) {
            trait.setPauseTicks(ticks);
            Messaging.sendTr(sender, Messages.PAUSEPATHFINDING_TICKS_SET, npc.getName(), ticks);
        }
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
            usage = "playerfilter -a(llowlist) -e(mpty) -d(enylist) --add [uuid] --remove [uuid] --addgroup [group] --removegroup [group] -c(lear) --applywithin [blocks range]",
            desc = "Sets the NPC filter",
            modifiers = { "playerfilter" },
            min = 1,
            max = 1,
            flags = "adce",
            permission = "citizens.npc.playerfilter")
    public void playerfilter(CommandContext args, CommandSender sender, NPC npc, @Flag("add") UUID add,
            @Flag("remove") UUID remove, @Flag("removegroup") String removegroup, @Flag("addgroup") String addgroup,
            @Flag("applywithin") Double applyRange) {
        PlayerFilter trait = npc.getOrAddTrait(PlayerFilter.class);
        if (add != null) {
            trait.addPlayer(add);
            Messaging.sendTr(sender, Messages.PLAYERFILTER_PLAYER_ADDED, add, npc.getName());
        }
        if (remove != null) {
            trait.removePlayer(remove);
            Messaging.sendTr(sender, Messages.PLAYERFILTER_PLAYER_REMOVED, remove, npc.getName());
        }
        if (addgroup != null) {
            trait.addGroup(addgroup);
            Messaging.sendTr(sender, Messages.PLAYERFILTER_GROUP_ADDED, addgroup, npc.getName());
        }
        if (removegroup != null) {
            trait.removeGroup(removegroup);
            Messaging.sendTr(sender, Messages.PLAYERFILTER_GROUP_REMOVED, removegroup, npc.getName());
        }
        if (applyRange != null) {
            trait.setApplyRange(applyRange);
            Messaging.sendTr(sender, Messages.PLAYERFILTER_APPLYRANGE_SET, npc.getName(), applyRange);
        }
        if (args.hasFlag('e')) {
            trait.setPlayers(Collections.emptySet());
            Messaging.sendTr(sender, Messages.PLAYERFILTER_EMPTY_SET, npc.getName());
        }
        if (args.hasFlag('a')) {
            trait.setAllowlist();
            Messaging.sendTr(sender, Messages.PLAYERFILTER_ALLOWLIST_SET, npc.getName());
        }
        if (args.hasFlag('d')) {
            trait.setDenylist();
            Messaging.sendTr(sender, Messages.PLAYERFILTER_DENYLIST_SET, npc.getName());
        }
        if (args.hasFlag('c')) {
            trait.clear();
            Messaging.sendTr(sender, Messages.PLAYERFILTER_CLEARED, npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "playerlist (-a(dd),r(emove))",
            desc = "Sets whether the NPC is put in the playerlist",
            modifiers = { "playerlist" },
            min = 1,
            max = 1,
            flags = "ar",
            permission = "citizens.npc.playerlist")
    @Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
    public void playerlist(CommandContext args, CommandSender sender, NPC npc) {
        boolean remove = !npc.data().get(NPC.Metadata.REMOVE_FROM_PLAYERLIST,
                Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
        if (args.hasFlag('a')) {
            remove = false;
        } else if (args.hasFlag('r')) {
            remove = true;
        }
        npc.data().setPersistent(NPC.Metadata.REMOVE_FROM_PLAYERLIST, remove);
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
            usage = "playsound [sound] (volume) (pitch) (--at x:y:z:world)",
            desc = "Plays a sound at the NPC's location",
            modifiers = { "playsound" },
            min = 2,
            max = 4,
            permission = "citizens.npc.playsound")
    @Requirements(selected = true, ownership = true)
    public void playsound(CommandContext args, CommandSender sender, NPC npc, @Arg(1) String sound,
            @Arg(value = 2, defValue = "1") Float volume, @Arg(value = 3, defValue = "1") Float pitch,
            @Flag("at") Location at) throws CommandException {
        Location loc = at == null ? npc.getStoredLocation() : at;
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    @Command(
            aliases = { "npc" },
            usage = "pose (--save [name] (-d) | --mirror [name] (-d) | --assume [name] | --remove [name] | --default [name]) (--yaw yaw) (--pitch pitch) (-a)",
            desc = "Manage NPC poses",
            flags = "ad",
            modifiers = { "pose" },
            min = 1,
            max = 2,
            permission = "citizens.npc.pose")
    public void pose(CommandContext args, CommandSender sender, NPC npc, @Flag("save") String save,
            @Flag("mirror") String mirror, @Flag("assume") String assume, @Flag("remove") String remove,
            @Flag("default") String defaultPose, @Flag("yaw") Float yaw, @Flag("pitch") Float pitch)
            throws CommandException {
        Poses trait = npc.getOrAddTrait(Poses.class);
        if (save != null) {
            if (save.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            Location loc = npc.getStoredLocation();
            if (yaw != null) {
                loc.setYaw(yaw);
            }
            if (pitch != null) {
                loc.setPitch(pitch);
            }
            if (trait.addPose(save, loc, args.hasFlag('d'))) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, save);
        } else if (mirror != null) {
            if (mirror.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(mirror, args.getSenderLocation(), args.hasFlag('d'))) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, mirror);
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
        if (args.hasFlag('a')) {
            if (args.getSenderLocation() == null)
                throw new ServerCommandException();
            trait.assumePose(args.getSenderLocation());
        }
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
        if (type != EntityType.VILLAGER && !type.name().equals("ZOMBIE_VILLAGER"))
            throw new CommandException(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE, Util.prettyEnum(type));

        if (parsed == null)
            throw new CommandException(Messages.INVALID_PROFESSION, args.getString(1),
                    Util.listValuesPretty(Profession.values()));

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
    public void remove(CommandContext args, CommandSender sender, NPC npc, @Flag("owner") String owner,
            @Flag("eid") UUID eid, @Flag("world") String world, @Arg(value = 1, completions = "all") String action)
            throws CommandException {
        if (owner != null) {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(owner);
            } catch (IllegalArgumentException ex) {
                try {
                    uuid = Bukkit.getOfflinePlayer(owner).getUniqueId();
                } catch (Exception e) {
                }
            }
            for (NPC rem : Lists.newArrayList(CitizensAPI.getNPCRegistry())) {
                if (!rem.getOrAddTrait(Owner.class).isOwnedBy(sender)) {
                    continue;
                }
                if (uuid != null && rem.getOrAddTrait(Owner.class).isOwnedBy(uuid)
                        || rem.getOrAddTrait(Owner.class).isOwnedBy(owner)) {
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
            } else {
                Messaging.sendErrorTr(sender, Messages.NPC_NOT_FOUND);
            }
            return;
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
            } else {
                NPCCommandSelector.Callback callback = npc1 -> {
                    if (npc1 == null)
                        throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
                    if (!(sender instanceof ConsoleCommandSender) && !npc1.getOrAddTrait(Owner.class).isOwnedBy(sender))
                        throw new CommandException(CommandMessages.MUST_BE_OWNER);
                    if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
                        throw new NoPermissionsException();
                    history.add(sender, new RemoveNPCHistoryItem(npc1));
                    npc1.destroy(sender);
                    Messaging.sendTr(sender, Messages.NPC_REMOVED, npc1.getName(), npc1.getId());
                };
                NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                        args.getString(1));
            }
            return;
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
        if (Placeholders.replace(Messaging.parseComponents(newName), sender, npc).length() > nameLength) {
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
    public void respawn(CommandContext args, CommandSender sender, NPC npc, @Arg(1) Duration delay) {
        if (delay != null) {
            npc.data().setPersistent(NPC.Metadata.RESPAWN_DELAY, Util.toTicks(delay));
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_SET, Util.toTicks(delay));
        } else {
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_DESCRIBE, npc.data().get(NPC.Metadata.RESPAWN_DELAY, -1));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "rotate (--towards [x,y,z]) (--body [yaw]) (--head [yaw]) (--pitch [pitch]) (-s(mooth))",
            desc = "Rotate NPC",
            flags = "s",
            modifiers = { "rotate" },
            min = 1,
            max = 1,
            permission = "citizens.npc.rotate")
    public void rotate(CommandContext args, CommandSender sender, NPC npc, @Flag("body") Float yaw,
            @Flag("head") Float head, @Flag("pitch") Float pitch, @Flag("towards") Location towards) {
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
        if (towards != null) {
            npc.getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToFace(towards);
            return;
        }
        if (yaw != null) {
            NMS.setBodyYaw(npc.getEntity(), yaw);
            if (npc.getEntity().getType() == EntityType.PLAYER) {
                NMS.sendPositionUpdateNearby(npc.getEntity(), true, yaw, npc.getEntity().getLocation().getPitch(),
                        null);
                PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
            }
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
            usage = "select|sel [id|name] (--range range) (--registry [name])",
            desc = "Select a NPC with the given ID or name",
            modifiers = { "select", "sel" },
            min = 1,
            max = 2,
            permission = "citizens.npc.select")
    @Requirements
    public void select(CommandContext args, CommandSender sender, NPC npc,
            @Flag(value = "range", defValue = "10") double range, @Flag("registry") String registryName)
            throws CommandException {
        NPCCommandSelector.Callback callback = toSelect -> {
            if (toSelect == null)
                throw new CommandException(Messages.NPC_NOT_FOUND);
            if (npc != null && toSelect.getId() == npc.getId())
                throw new CommandException(Messages.NPC_ALREADY_SELECTED);
            selector.select(sender, toSelect);
            Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
        };

        NPCRegistry registry = registryName != null ? CitizensAPI.getNamedNPCRegistry(registryName)
                : CitizensAPI.getNPCRegistry();
        if (registry == null)
            throw new CommandException(Messages.UNKNOWN_NPC_REGISTRY, args.getFlag("registry"));

        if (args.argsLength() <= 1) {
            if (args.getSenderLocation() == null)
                throw new ServerCommandException();
            Location location = args.getSenderLocation();
            List<NPC> search = location.getWorld().getNearbyEntities(location, range, range, range).stream()
                    .map(e -> registry.getNPC(e)).filter(e -> e != null).collect(Collectors.toList());
            Collections.sort(search, (o1, o2) -> Double.compare(o1.getEntity().getLocation().distanceSquared(location),
                    o2.getEntity().getLocation().distanceSquared(location)));
            for (NPC test : search) {
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
            usage = "setequipment [slot] [item]",
            desc = "Sets equipment via commands",
            modifiers = { "setequipment" },
            min = 2,
            max = 3,
            permission = "citizens.npc.setequipment")
    public void setequipment(CommandContext args, CommandSender sender, NPC npc, @Arg(1) EquipmentSlot slot,
            @Arg(2) ItemStack item) throws CommandException {
        if (slot == null)
            throw new CommandUsageException();

        if (item == null && args.argsLength() == 3 && args.getString(2).equalsIgnoreCase("hand")) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            item = ((Player) sender).getItemInHand().clone();
        }
        npc.getOrAddTrait(Equipment.class).set(slot, item);
        Messaging.sendTr(sender, Messages.EQUIPMENT_SET, slot, item);
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
        if (!hasArg)
            throw new CommandException();
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
        if (shop == null)
            throw new CommandUsageException();

        if (action.equalsIgnoreCase("delete")) {
            if (args.argsLength() != 3)
                throw new CommandUsageException();
            if (!shop.canEdit(npc, sender))
                throw new NoPermissionsException();
            shops.deleteShop(shop);
        } else if (action.equalsIgnoreCase("edit")) {
            if (!shop.canEdit(npc, sender))
                throw new NoPermissionsException();
            shop.displayEditor(npc == null ? null : npc.getOrAddTrait(ShopTrait.class), sender);
        } else if (action.equalsIgnoreCase("show")) {
            shop.display(sender);
        } else
            throw new CommandUsageException();
    }

    @Command(
            aliases = { "npc" },
            usage = "sitting (--explicit [true|false]) (--at [at])",
            desc = "Sets the NPC sitting",
            modifiers = { "sitting" },
            min = 1,
            max = 2,
            permission = "citizens.npc.sitting")
    @Requirements(selected = true, ownership = true)
    public void sitting(CommandContext args, CommandSender sender, NPC npc, @Flag("explicit") Boolean explicit,
            @Flag("at") Location at) {
        SitTrait trait = npc.getOrAddTrait(SitTrait.class);
        boolean toSit = explicit != null ? explicit : !trait.isSitting();
        if (!toSit) {
            trait.setSitting(null);
            Messaging.sendTr(sender, Messages.SITTING_UNSET, npc.getName());
            return;
        }
        if (at == null) {
            at = npc.getStoredLocation();
        }
        trait.setSitting(at);
        Messaging.sendTr(sender, Messages.SITTING_SET, npc.getName(), Util.prettyPrintLocation(at));
    }

    @Command(
            aliases = { "npc" },
            usage = "skin (-e(xport) -c(lear) -l(atest) -s(kull)) [name] (or --url [url] --file [file] (-s(lim)) or -t [uuid/name] [data] [signature])",
            desc = "Sets an NPC's skin name. Use -l to set the skin to always update to the latest",
            modifiers = { "skin" },
            min = 1,
            max = 4,
            flags = "ectls",
            permission = "citizens.npc.skin")
    @Requirements(types = EntityType.PLAYER, selected = true, ownership = true)
    public void skin(CommandContext args, CommandSender sender, NPC npc, @Flag("url") String url,
            @Flag("file") String file) throws CommandException {
        String skinName = npc.getName();
        SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);
        if (args.hasFlag('c')) {
            trait.clearTexture();
            Messaging.sendTr(sender, Messages.SKIN_CLEARED);
            return;
        } else if (args.hasFlag('e')) {
            if (trait.getTexture() == null)
                throw new CommandException(Messages.SKIN_REQUIRED);

            File skinsFolder = new File(CitizensAPI.getDataFolder(), "skins");
            File skin = file == null ? new File(skinsFolder, npc.getUniqueId().toString() + ".png")
                    : new File(skinsFolder, file);

            if (!skin.getParentFile().equals(skinsFolder) || !skin.getName().endsWith(".png"))
                throw new CommandException(Messages.INVALID_SKIN_FILE, file);

            try {
                JSONObject data = (JSONObject) new JSONParser()
                        .parse(new String(BaseEncoding.base64().decode(trait.getTexture())));
                JSONObject textures = (JSONObject) data.get("textures");
                JSONObject skinObj = (JSONObject) textures.get("SKIN");
                URL textureUrl = new URL(skinObj.get("url").toString().replace("\\", ""));

                if (!textureUrl.getHost().equals("textures.minecraft.net"))
                    throw new CommandException(Messages.ERROR_SETTING_SKIN_URL, "Mojang");

                try (ReadableByteChannel in = Channels.newChannel(textureUrl.openStream());
                        FileOutputStream out = new FileOutputStream(skin)) {
                    out.getChannel().transferFrom(in, 0, 10000);
                }
                Messaging.send(sender, Messages.SKIN_EXPORTED, skin.getName());
            } catch (Exception e) {
                throw new CommandException("Couldn't parse texture: " + e.getMessage());
            }
            return;
        } else if (url != null || file != null) {
            Messaging.sendTr(sender, Messages.FETCHING_SKIN, url == null ? file : url);
            Bukkit.getScheduler().runTaskAsynchronously(CitizensAPI.getPlugin(), () -> {
                try {
                    JSONObject data = null;
                    if (file != null) {
                        File skinsFolder = new File(CitizensAPI.getDataFolder(), "skins");
                        File skin = new File(skinsFolder, Placeholders.replace(file, sender, npc));
                        if (!skin.exists() || !skin.isFile() || skin.isHidden()
                                || !skin.getParentFile().equals(skinsFolder)) {
                            Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(),
                                    () -> Messaging.sendErrorTr(sender, Messages.INVALID_SKIN_FILE, file));
                            return;
                        }
                        data = MojangSkinGenerator.generateFromPNG(Files.readAllBytes(skin.toPath()),
                                args.hasFlag('s'));
                    } else {
                        data = MojangSkinGenerator.generateFromURL(Placeholders.replace(url, sender, npc),
                                args.hasFlag('s'));
                    }
                    String uuid = (String) data.get("uuid");
                    JSONObject texture = (JSONObject) data.get("texture");
                    String textureEncoded = (String) texture.get("value");
                    String signature = (String) texture.get("signature");

                    Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), () -> {
                        try {
                            trait.setSkinPersistent(uuid, signature, textureEncoded);
                            Messaging.sendTr(sender, Messages.SKIN_URL_SET, npc.getName(), url == null ? file : url);
                        } catch (IllegalArgumentException e) {
                            Messaging.sendErrorTr(sender, Messages.ERROR_SETTING_SKIN_URL, url == null ? file : url);
                        }
                    });
                } catch (Throwable t) {
                    if (Messaging.isDebugging()) {
                        t.printStackTrace();
                    }
                    Bukkit.getScheduler().runTask(CitizensAPI.getPlugin(), () -> Messaging.sendErrorTr(sender,
                            Messages.ERROR_SETTING_SKIN_URL, url == null ? file : url));
                }
            });
            return;
        } else if (args.hasFlag('t')) {
            if (args.argsLength() != 4)
                throw new CommandException(Messages.SKIN_REQUIRED);

            trait.setSkinPersistent(args.getString(1), args.getString(3), args.getString(2));
            Messaging.sendTr(sender, Messages.SKIN_SET, npc.getName(), args.getString(1));
            return;
        } else if (args.hasFlag('s') && npc.getEntity() instanceof Player) {
            ItemStack is = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) is.getItemMeta();
            NMS.setProfile(sm, NMS.getProfile((Player) npc.getEntity()));
            is.setItemMeta(sm);
            if (sender instanceof Player && ((Player) sender).getInventory().addItem(is).isEmpty()) {
            } else if (args.getSenderLocation() != null) {
                args.getSenderLocation().getWorld().dropItem(args.getSenderLocation(), is);
            } else
                throw new ServerCommandException();
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
    public void skinLayers(CommandContext args, CommandSender sender, NPC npc, @Flag("cape") Boolean cape,
            @Flag("hat") Boolean hat, @Flag("jacket") Boolean jacket, @Flag("sleeves") Boolean sleeves,
            @Flag("pants") Boolean pants) throws CommandException {
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
    public void sound(CommandContext args, CommandSender sender, NPC npc, @Flag("death") Sound death,
            @Flag("ambient") Sound ambient, @Flag("hurt") Sound hurt) throws CommandException {
        String ambientSound = npc.data().get(NPC.Metadata.AMBIENT_SOUND);
        String deathSound = npc.data().get(NPC.Metadata.DEATH_SOUND);
        String hurtSound = npc.data().get(NPC.Metadata.HURT_SOUND);
        if (args.getValueFlags().size() == 0 && args.getFlags().size() == 0) {
            Messaging.sendTr(sender, Messages.SOUND_INFO, npc.getName(), ambientSound, hurtSound, deathSound);
            return;
        }
        if (args.hasFlag('n')) {
            ambientSound = deathSound = hurtSound = "";
            npc.data().setPersistent(NPC.Metadata.SILENT, true);
        }
        if (args.hasFlag('s')) {
            npc.data().setPersistent(NPC.Metadata.SILENT, !npc.data().get(NPC.Metadata.SILENT, false));
        }
        if (args.hasFlag('d')) {
            ambientSound = deathSound = hurtSound = null;
            npc.data().setPersistent(NPC.Metadata.SILENT, false);
        } else {
            if (death != null) {
                deathSound = NMS.getSoundPath(death);
            } else if (args.hasValueFlag("death")) {
                deathSound = args.getFlag("death").equals("d") ? null : args.getFlag("death");
            }
            if (ambient != null) {
                ambientSound = NMS.getSoundPath(ambient);
            } else if (args.hasValueFlag("ambient")) {
                ambientSound = args.getFlag("ambient").equals("d") ? null : args.getFlag("ambient");
            }
            if (hurt != null) {
                hurtSound = NMS.getSoundPath(hurt);
            } else if (args.hasValueFlag("hurt")) {
                hurtSound = args.getFlag("hurt").equals("d") ? null : args.getFlag("hurt");
            }
        }
        if (deathSound == null) {
            npc.data().remove(NPC.Metadata.DEATH_SOUND);
        } else {
            npc.data().setPersistent(NPC.Metadata.DEATH_SOUND, deathSound);
        }
        if (hurtSound == null) {
            npc.data().remove(NPC.Metadata.HURT_SOUND);
        } else {
            npc.data().setPersistent(NPC.Metadata.HURT_SOUND, hurtSound);
        }
        if (ambientSound == null) {
            npc.data().remove(NPC.Metadata.AMBIENT_SOUND);
        } else {
            npc.data().setPersistent(NPC.Metadata.AMBIENT_SOUND, ambientSound);
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
        if (!Strings.isNullOrEmpty(ambientSound) && !ambientSound.equals("none")
                || !Strings.isNullOrEmpty(deathSound) && !deathSound.equals("none")
                || !Strings.isNullOrEmpty(hurtSound) && !hurtSound.equals("none")) {
            npc.data().setPersistent(NPC.Metadata.SILENT, false);
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
    public void spawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPCCommandSelector.Callback callback = respawn -> {
            if (respawn == null) {
                if (args.argsLength() > 1)
                    throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getString(1));
                else
                    throw new CommandException(CommandMessages.MUST_HAVE_SELECTED);
            }
            if (respawn.isSpawned())
                throw new CommandException(Messages.NPC_ALREADY_SPAWNED, respawn.getName());
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
            usage = "speak [message] --bubble [duration] --target [npcid|player name] --range (range to look for entities to speak to in blocks)",
            desc = "Says a message from the NPC",
            modifiers = { "speak" },
            min = 2,
            permission = "citizens.npc.speak")
    public void speak(CommandContext args, CommandSender sender, NPC npc, @Flag("bubble") Duration bubbleDuration,
            @Flag("type") String type, @Flag("target") String target, @Flag("range") Float range)
            throws CommandException {
        String message = args.getJoinedStrings(1);

        SpeechContext context = new SpeechContext(message);

        Player playerRecipient = null;
        if (target != null) {
            if (target.matches("\\d+")) {
                NPC targetNPC = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(args.getFlag("target")));
                if (targetNPC != null) {
                    context.addRecipient(targetNPC.getEntity());
                }
            } else {
                Player player = Bukkit.getPlayerExact(target);
                if (player != null) {
                    context.addRecipient(player);
                    playerRecipient = player;
                }
            }
        }
        if (bubbleDuration != null) {
            HologramTrait trait = npc.getOrAddTrait(HologramTrait.class);
            trait.addTemporaryLine(Placeholders.replace(message, playerRecipient, npc), Util.toTicks(bubbleDuration));
            return;
        }
        if (range != null) {
            npc.getEntity().getNearbyEntities(range, range, range).forEach(e -> {
                if (!CitizensAPI.getNPCRegistry().isNPC(e)) {
                    context.addRecipient(e);
                }
            });
        }
        npc.getDefaultSpeechController().speak(context);
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
        boolean targetable = !npc.data().get(NPC.Metadata.TARGETABLE, !npc.isProtected());
        if (args.hasFlag('t')) {
            npc.data().set(NPC.Metadata.TARGETABLE, targetable);
        } else {
            npc.data().setPersistent(NPC.Metadata.TARGETABLE, targetable);
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
            usage = "tphere (cursor) -c(enter) -f(ront)",
            desc = "Teleport a NPC to your location",
            flags = "cf",
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
                && npc.getStoredLocation().getWorld() != args.getSenderLocation().getWorld())
            throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
        if (args.hasFlag('c')) {
            to = to.getBlock().getLocation();
            to.setX(to.getX() + 0.5);
            to.setZ(to.getZ() + 0.5);
        }
        if (args.hasFlag('f')) {
            to = to.clone().add(to.getDirection().setY(0));
            to.setDirection(to.getDirection().multiply(-1)).setPitch(0);
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
            npc.data().set(NPC.Metadata.DEFAULT_PROTECTED, vulnerable);
        } else {
            npc.data().setPersistent(NPC.Metadata.DEFAULT_PROTECTED, vulnerable);
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
        if (sender instanceof Player && Editor.hasEditor((Player) sender)) {
            Editor.leave((Player) sender);
        }
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
