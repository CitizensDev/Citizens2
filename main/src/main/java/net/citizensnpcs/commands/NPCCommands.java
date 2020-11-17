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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mysql.jdbc.StringUtils;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.RequirementMissingException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.event.CommandSenderCloneNPCEvent;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.PlayerCloneNPCEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.CommandTrait.ExecutionMode;
import net.citizensnpcs.trait.CommandTrait.NPCCommandBuilder;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
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
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.ScriptTrait;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.SkinLayers;
import net.citizensnpcs.trait.SkinLayers.Layer;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WitherTrait;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final NPCSelector selector;

    public NPCCommands(Citizens plugin) {
        selector = plugin.getNPCSelector();
    }

    @Command(
            aliases = { "npc" },
            usage = "age [age] (-l)",
            desc = "Set the age of a NPC",
            help = Messages.COMMAND_AGE_HELP,
            flags = "l",
            modifiers = { "age" },
            min = 1,
            max = 2,
            permission = "citizens.npc.age")
    public void age(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.isSpawned() || (!(npc.getEntity() instanceof Ageable) && !(npc.getEntity() instanceof Zombie)))
            throw new CommandException(Messages.MOBTYPE_CANNOT_BE_AGED, npc.getName());
        Age trait = npc.getOrAddTrait(Age.class);
        boolean toggleLock = args.hasFlag('l');
        if (toggleLock) {
            Messaging.sendTr(sender, trait.toggle() ? Messages.AGE_LOCKED : Messages.AGE_UNLOCKED);
        }
        if (args.argsLength() <= 1) {
            if (!toggleLock)
                trait.describe(sender);
            return;
        }
        int age = 0;
        try {
            age = args.getInteger(1);
            if (age > 0) {
                throw new CommandException(Messages.INVALID_AGE);
            }
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
    public void ai(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean useAI = npc.useMinecraftAI();
        if (args.argsLength() == 1) {
            useAI = !useAI;
        } else {
            useAI = Boolean.parseBoolean(args.getString(1));
        }
        npc.setUseMinecraftAI(useAI);
        Messaging.sendTr(sender, useAI ? Messages.USING_MINECRAFT_AI : Messages.NOT_USING_MINECRAFT_AI);
    }

    @Command(
            aliases = { "npc" },
            usage = "anchor (--save [name]|--assume [name]|--remove [name]) (-a)(-c)",
            desc = "Changes/Saves/Lists NPC's location anchor(s)",
            flags = "ac",
            modifiers = { "anchor" },
            min = 1,
            max = 3,
            permission = "citizens.npc.anchor")
    public void anchor(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Anchors trait = npc.getOrAddTrait(Anchors.class);
        if (args.hasValueFlag("save")) {
            if (args.getFlag("save").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (args.hasFlag('c')) {
                if (trait.addAnchor(args.getFlag("save"), args.getSenderTargetBlockLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, args.getFlag("save"));
            } else {
                if (trait.addAnchor(args.getFlag("save"), args.getSenderLocation())) {
                    Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
                } else
                    throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, args.getFlag("save"));
            }
        } else if (args.hasValueFlag("assume")) {
            if (args.getFlag("assume").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            Anchor anchor = trait.getAnchor(args.getFlag("assume"));
            if (anchor == null)
                throw new CommandException(Messages.ANCHOR_MISSING, args.getFlag("assume"));
            npc.teleport(anchor.getLocation(), TeleportCause.COMMAND);
        } else if (args.hasValueFlag("remove")) {
            if (args.getFlag("remove").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);
            if (trait.removeAnchor(trait.getAnchor(args.getFlag("remove"))))
                Messaging.sendTr(sender, Messages.ANCHOR_REMOVED);
            else
                throw new CommandException(Messages.ANCHOR_MISSING, args.getFlag("remove"));
        } else if (!args.hasFlag('a')) {
            Paginator paginator = new Paginator().header("Anchors").console(sender instanceof ConsoleCommandSender);
            paginator.addLine("<e>Key: <a>ID  <b>Name  <c>World  <d>Location (X,Y,Z)");
            for (int i = 0; i < trait.getAnchors().size(); i++) {
                if (trait.getAnchors().get(i).isLoaded()) {
                    String line = "<a>" + i + "<b>  " + trait.getAnchors().get(i).getName() + "<c>  "
                            + trait.getAnchors().get(i).getLocation().getWorld().getName() + "<d>  "
                            + trait.getAnchors().get(i).getLocation().getBlockX() + ", "
                            + trait.getAnchors().get(i).getLocation().getBlockY() + ", "
                            + trait.getAnchors().get(i).getLocation().getBlockZ();
                    paginator.addLine(line);
                } else {
                    String[] parts = trait.getAnchors().get(i).getUnloadedValue();
                    String line = "<a>" + i + "<b>  " + trait.getAnchors().get(i).getName() + "<c>  " + parts[0]
                            + "<d>  " + parts[1] + ", " + parts[2] + ", " + parts[3] + " <f>(unloaded)";
                    paginator.addLine(line);
                }
            }

            int page = args.getInteger(1, 1);
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
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
            usage = "armorstand --visible [visible] --small [small] --gravity [gravity] --arms [arms] --baseplate [baseplate]",
            desc = "Edit armorstand properties",
            modifiers = { "armorstand" },
            min = 1,
            max = 1)
    @Requirements(selected = true, ownership = true, types = EntityType.ARMOR_STAND)
    public void armorstand(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ArmorStandTrait trait = npc.getOrAddTrait(ArmorStandTrait.class);
        if (args.hasValueFlag("visible")) {
            trait.setVisible(Boolean.valueOf(args.getFlag("visible")));
        }
        if (args.hasValueFlag("small")) {
            trait.setSmall(Boolean.valueOf(args.getFlag("small")));
        }
        if (args.hasValueFlag("gravity")) {
            trait.setGravity(Boolean.valueOf(args.getFlag("gravity")));
        }
        if (args.hasValueFlag("arms")) {
            trait.setHasArms(Boolean.valueOf(args.getFlag("arms")));
        }
        if (args.hasValueFlag("baseplate")) {
            trait.setHasBaseplate(Boolean.valueOf(args.getFlag("baseplate")));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "collidable",
            desc = "Toggles an NPC's collidability",
            modifiers = { "collidable" },
            min = 1,
            max = 1,
            permission = "citizens.npc.collidable")
    @Requirements(ownership = true, selected = true, types = { EntityType.PLAYER })
    public void collidable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        npc.data().setPersistent(NPC.COLLIDABLE_METADATA, !npc.data().get(NPC.COLLIDABLE_METADATA, true));
        Messaging.sendTr(sender,
                npc.data().<Boolean> get(NPC.COLLIDABLE_METADATA) ? Messages.COLLIDABLE_SET : Messages.COLLIDABLE_UNSET,
                npc.getName());

    }

    @Command(
            aliases = { "npc" },
            usage = "command|cmd (add [command] | remove [id] | permissions [permissions] | sequential | random | cost) (-l[eft]/-r[ight]) (-p[layer] -o[p]), --cooldown [seconds] --delay [ticks] --permissions [perms] --n [max # of uses]",
            desc = "Controls commands which will be run when clicking on an NPC",
            help = Messages.NPC_COMMAND_HELP,
            modifiers = { "command", "cmd" },
            min = 1,
            flags = "lrpo",
            permission = "citizens.npc.command")
    public void command(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        CommandTrait commands = npc.getOrAddTrait(CommandTrait.class);
        if (args.argsLength() == 1) {
            commands.describe(sender);
        } else if (args.getString(1).equalsIgnoreCase("add")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();
            String command = args.getJoinedStrings(2);
            CommandTrait.Hand hand = args.hasFlag('l') && args.hasFlag('r') ? CommandTrait.Hand.BOTH
                    : args.hasFlag('l') ? CommandTrait.Hand.LEFT : CommandTrait.Hand.RIGHT;
            List<String> perms = Lists.newArrayList();
            if (args.hasValueFlag("permissions")) {
                perms.addAll(Arrays.asList(args.getFlag("permissions").split(",")));
            }
            int id = commands.addCommand(
                    new NPCCommandBuilder(command, hand).addPerms(perms).player(args.hasFlag('p') || args.hasFlag('o'))
                            .op(args.hasFlag('o')).cooldown(args.getFlagInteger("cooldown", 0))
                            .n(args.getFlagInteger("n", -1)).delay(args.getFlagInteger("delay", 0)));
            Messaging.sendTr(sender, Messages.COMMAND_ADDED, command, id);
        } else if (args.getString(1).equalsIgnoreCase("sequential")) {
            commands.setExecutionMode(commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? ExecutionMode.LINEAR
                    : ExecutionMode.SEQUENTIAL);
            Messaging.sendTr(sender,
                    commands.getExecutionMode() == ExecutionMode.SEQUENTIAL ? Messages.COMMANDS_SEQUENTIAL_SET
                            : Messages.COMMANDS_SEQUENTIAL_UNSET);
        } else if (args.getString(1).equalsIgnoreCase("remove")) {
            if (args.argsLength() == 2)
                throw new CommandUsageException();
            int id = args.getInteger(2);
            if (!commands.hasCommandId(id))
                throw new CommandException(Messages.COMMAND_UNKNOWN_COMMAND_ID, id);
            commands.removeCommandById(id);
            Messaging.sendTr(sender, Messages.COMMAND_REMOVED, id);
        } else if (args.getString(1).equalsIgnoreCase("permissions") || args.getString(1).equalsIgnoreCase("perms")) {
            List<String> temporaryPermissions = Arrays.asList(args.getSlice(2));
            commands.setTemporaryPermissions(temporaryPermissions);
            Messaging.sendTr(sender, Messages.COMMAND_TEMPORARY_PERMISSIONS_SET,
                    Joiner.on(' ').join(temporaryPermissions));
        } else if (args.getString(1).equalsIgnoreCase("cost")) {
            commands.setCost(args.getDouble(2));
            Messaging.sendTr(sender, Messages.COMMAND_COST_SET, args.getDouble(2));
        } else if (args.getString(1).equalsIgnoreCase("random")) {
            commands.setExecutionMode(
                    commands.getExecutionMode() == ExecutionMode.RANDOM ? ExecutionMode.LINEAR : ExecutionMode.RANDOM);
            Messaging.sendTr(sender, commands.getExecutionMode() == ExecutionMode.RANDOM ? Messages.COMMANDS_RANDOM_SET
                    : Messages.COMMANDS_RANDOM_UNSET);
        } else {
            throw new CommandUsageException();
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "controllable|control (-m(ount),-y,-n,-o)",
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
    public void copy(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = args.getFlag("name", npc.getFullName());
        NPC copy = npc.clone();
        if (!copy.getFullName().equals(name)) {
            copy.setName(name);
        }

        if (copy.isSpawned() && args.getSenderLocation() != null) {
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
    }

    @Command(
            aliases = { "npc" },
            usage = "create [name] ((-b(aby),u(nspawned),s(ilent)) --at [x:y:z:world] --type [type] --trait ['trait1, trait2...'] --b [behaviours])",
            desc = "Create a new NPC",
            flags = "bus",
            modifiers = { "create" },
            min = 2,
            permission = "citizens.npc.create")
    @Requirements
    public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = Colorizer.parseColors(args.getJoinedStrings(1).trim());
        EntityType type = EntityType.PLAYER;
        if (args.hasValueFlag("type")) {
            String inputType = args.getFlag("type");
            type = Util.matchEntityType(inputType);
            if (type == null) {
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_INVALID_MOBTYPE, inputType));
            } else if (!EntityControllers.controllerExistsForType(type)) {
                throw new CommandException(Messaging.tr(Messages.NPC_CREATE_MISSING_MOBTYPE, inputType));
            }
        }

        int nameLength = SpigotUtil.getMaxNameLength(type);
        if (name.length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG, nameLength);
            name = name.substring(0, nameLength);
        }
        if (name.length() == 0)
            throw new CommandException();

        if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall")
                && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", "")))
            throw new NoPermissionsException();

        npc = CitizensAPI.getNPCRegistry().createNPC(type, name);
        String msg = "You created [[" + npc.getName() + "]] (ID [[" + npc.getId() + "]])";

        int age = 0;
        if (args.hasFlag('b')) {
            if (!Ageable.class.isAssignableFrom(type.getEntityClass()))
                Messaging.sendErrorTr(sender, Messages.MOBTYPE_CANNOT_BE_AGED,
                        type.name().toLowerCase().replace("_", "-"));
            else {
                age = -24000;
                msg += " as a baby";
            }
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

        if (args.hasValueFlag("at")) {
            spawnLoc = CommandContext.parseLocation(args.getSenderLocation(), args.getFlag("at"));
            spawnLoc.getChunk().load();
        }

        if (spawnLoc == null) {
            npc.destroy();
            throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
        }

        if (!args.hasFlag('u')) {
            npc.spawn(spawnLoc, SpawnReason.CREATE);
        }

        if (args.hasValueFlag("trait")) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("trait"));
            StringBuilder builder = new StringBuilder();
            for (String tr : parts) {
                Trait trait = CitizensAPI.getTraitFactory().getTrait(tr);
                if (trait == null)
                    continue;
                npc.addTrait(trait);
                builder.append(StringHelper.wrap(tr) + ", ");
            }
            if (builder.length() > 0)
                builder.delete(builder.length() - 2, builder.length());
            msg += " with traits " + builder.toString();
        }

        if (args.hasValueFlag("template")) {
            Iterable<String> parts = Splitter.on(',').trimResults().split(args.getFlag("template"));
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                Template template = Template.byName(part);
                if (template == null)
                    continue;
                template.apply(npc);
                builder.append(StringHelper.wrap(part) + ", ");
            }
            if (builder.length() > 0)
                builder.delete(builder.length() - 2, builder.length());
            msg += " with templates " + builder.toString();
        }

        // Set age after entity spawns
        if (npc.getEntity() instanceof Ageable) {
            npc.getOrAddTrait(Age.class).setAge(age);
        }
        selector.select(sender, npc);
        Messaging.send(sender, msg + '.');
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
                throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
            }
            NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                    args.getString(1));
        } else {
            callback.run(npc);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "enderman -a[ngry]",
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
    public void flyable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean flyable = args.argsLength() == 2 ? args.getString(1).equals("true") : !npc.isFlyable();
        npc.setFlyable(flyable);
        flyable = npc.isFlyable(); // may not have applied, eg bats always flyable
        Messaging.sendTr(sender, flyable ? Messages.FLYABLE_SET : Messages.FLYABLE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "follow (player name) (-p[rotect])",
            desc = "Toggles NPC following you",
            flags = "p",
            modifiers = { "follow" },
            min = 1,
            max = 2,
            permission = "citizens.npc.follow")
    public void follow(CommandContext args, Player sender, NPC npc) throws CommandException {
        boolean protect = args.hasFlag('p');
        String name = sender.getName();
        if (args.argsLength() > 1) {
            name = args.getString(1);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player == null) {
            throw new CommandException();
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
    public void gamemode(CommandContext args, CommandSender sender, NPC npc) {
        Player player = (Player) npc.getEntity();
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.GAMEMODE_DESCRIBE, npc.getName(),
                    player.getGameMode().name().toLowerCase());
            return;
        }
        GameMode mode = null;
        try {
            int value = args.getInteger(1);
            mode = GameMode.getByValue(value);
        } catch (NumberFormatException ex) {
            try {
                mode = GameMode.valueOf(args.getString(1).toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        if (mode == null) {
            Messaging.sendErrorTr(sender, Messages.GAMEMODE_INVALID, args.getString(1));
            return;
        }
        npc.getOrAddTrait(GameModeTrait.class).setGameMode(mode);
        Messaging.sendTr(sender, Messages.GAMEMODE_SET, mode.name().toLowerCase());
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
    public void glowing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.hasValueFlag("color")) {
            ChatColor chatColor = Util.matchEnum(ChatColor.values(), args.getFlag("color"));
            if (!(npc.getEntity() instanceof Player))
                throw new CommandException(Messages.GLOWING_COLOR_PLAYER_ONLY);
            npc.getOrAddTrait(ScoreboardTrait.class).setColor(chatColor);
            if (!npc.data().has(NPC.GLOWING_METADATA)) {
                npc.data().setPersistent(NPC.GLOWING_METADATA, true);
            }
            Messaging.sendTr(sender, Messages.GLOWING_COLOR_SET, npc.getName(),
                    chatColor == null ? ChatColor.WHITE + "white" : chatColor + Util.prettyEnum(chatColor));
            return;
        }
        npc.data().setPersistent(NPC.GLOWING_METADATA, !npc.data().get(NPC.GLOWING_METADATA, false));
        boolean glowing = npc.data().get(NPC.GLOWING_METADATA);
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
        boolean enabled = npc.getOrAddTrait(Gravity.class).toggle();
        String key = !enabled ? Messages.GRAVITY_ENABLED : Messages.GRAVITY_DISABLED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "hologram add [text] | set [line #] [text] | remove [line #] | clear | lineheight [height] | direction [up|down]",
            desc = "Controls NPC hologram text",
            modifiers = { "hologram" },
            min = 1,
            max = -1,
            permission = "citizens.npc.hologram")
    public void hologram(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
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

        if (args.getString(1).equalsIgnoreCase("set")) {
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
        } else if (args.getString(1).equalsIgnoreCase("add")) {
            if (args.argsLength() == 2) {
                throw new CommandException(Messages.HOLOGRAM_TEXT_MISSING);
            }
            trait.addLine(args.getJoinedStrings(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_ADD, args.getJoinedStrings(2));
        } else if (args.getString(1).equalsIgnoreCase("remove")) {
            if (args.argsLength() == 2) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }
            int idx = Math.max(0, args.getInteger(2));
            if (idx > trait.getLines().size()) {
                throw new CommandException(Messages.HOLOGRAM_INVALID_LINE);
            }
            trait.removeLine(idx);
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_REMOVED, idx);
        } else if (args.getString(1).equalsIgnoreCase("clear")) {
            trait.clear();
            Messaging.sendTr(sender, Messages.HOLOGRAM_CLEARED);
        } else if (args.getString(1).equalsIgnoreCase("lineheight")) {
            trait.setLineHeight(args.getDouble(2));
            Messaging.sendTr(sender, Messages.HOLOGRAM_LINE_HEIGHT_SET, args.getDouble(2));
        } else if (args.getString(1).equalsIgnoreCase("direction")) {
            HologramDirection direction = args.getString(2).equalsIgnoreCase("up") ? HologramDirection.BOTTOM_UP
                    : HologramDirection.TOP_DOWN;
            trait.setDirection(direction);
            Messaging.sendTr(sender, Messages.HOLOGRAM_DIRECTION_SET, Util.prettyEnum(direction));
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "horse|llama|donkey|mule (--color color) (--type type) (--style style) (-cb)",
            desc = "Sets horse and horse-like entity modifiers",
            help = "Use the -c flag to make the NPC have a chest, or the -b flag to stop them from having a chest.",
            modifiers = { "horse", "llama", "donkey", "mule" },
            min = 1,
            max = 1,
            flags = "cb",
            permission = "citizens.npc.horse")
    @Requirements(selected = true, ownership = true)
    public void horse(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
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
            String colorRaw = args.getFlag("color", args.getFlag("colour"));
            Horse.Color color = Util.matchEnum(Horse.Color.values(), colorRaw);
            if (color == null) {
                String valid = Util.listValuesPretty(Horse.Color.values());
                throw new CommandException(Messages.INVALID_HORSE_COLOR, valid);
            }
            horse.setColor(color);
            output += Messaging.tr(Messages.HORSE_COLOR_SET, Util.prettyEnum(color));
        }
        if (type == EntityType.HORSE && args.hasValueFlag("style")) {
            Horse.Style style = Util.matchEnum(Horse.Style.values(), args.getFlag("style"));
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
            sender.sendMessage(output);
        }
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
        Messaging.send(sender, npc.getId());
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
            usage = "item [item] (data)",
            desc = "Sets the NPC's item",
            modifiers = { "item", },
            min = 2,
            max = 3,
            flags = "",
            permission = "citizens.npc.item")
    @Requirements(
            selected = true,
            ownership = true,
            types = { EntityType.DROPPED_ITEM, EntityType.ITEM_FRAME, EntityType.FALLING_BLOCK })
    public void item(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Material mat = Material.matchMaterial(args.getString(1));
        if (mat == null)
            throw new CommandException(Messages.UNKNOWN_MATERIAL);
        int data = args.getInteger(2, 0);
        npc.data().setPersistent(NPC.ITEM_ID_METADATA, mat.name());
        npc.data().setPersistent(NPC.ITEM_DATA_METADATA, data);
        switch (npc.getEntity().getType()) {
            case DROPPED_ITEM:
                ((org.bukkit.entity.Item) npc.getEntity()).getItemStack().setType(mat);
                break;
            case ITEM_FRAME:
                ((ItemFrame) npc.getEntity()).getItem().setType(mat);
                break;
            default:
                break;
        }
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        }
        Messaging.sendTr(sender, Messages.ITEM_SET, Util.prettyEnum(mat));
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
    public void list(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPCRegistry source = args.hasValueFlag("registry") ? CitizensAPI.getNamedNPCRegistry(args.getFlag("registry"))
                : CitizensAPI.getNPCRegistry();
        if (source == null)
            throw new CommandException();
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : source.sorted()) {
                npcs.add(add);
            }
        } else if (args.getValueFlags().size() == 0 && sender instanceof Player) {
            for (NPC add : source.sorted()) {
                if (!npcs.contains(add) && add.getOrAddTrait(Owner.class).isOwnedBy(sender)) {
                    npcs.add(add);
                }
            }
        } else {
            if (args.hasValueFlag("owner")) {
                String name = args.getFlag("owner");
                for (NPC add : source.sorted()) {
                    if (!npcs.contains(add) && add.getOrAddTrait(Owner.class).isOwnedBy(name)) {
                        npcs.add(add);
                    }
                }
            }

            if (args.hasValueFlag("type")) {
                EntityType type = Util.matchEntityType(args.getFlag("type"));

                if (type == null)
                    throw new CommandException(Messages.COMMAND_INVALID_MOBTYPE, type);

                for (NPC add : source) {
                    if (!npcs.contains(add) && add.getOrAddTrait(MobType.class).getType() == type)
                        npcs.add(add);
                }
            }
        }

        Paginator paginator = new Paginator().header("NPCs").console(sender instanceof ConsoleCommandSender);
        paginator.addLine("<e><o>ID  <a><o>Name");
        for (int i = 0; i < npcs.size(); i += 2) {
            String line = "<e>" + npcs.get(i).getId() + "<a>  " + npcs.get(i).getName();
            if (npcs.size() >= i + 2)
                line += "      " + "<e>" + npcs.get(i + 1).getId() + "<a>  " + npcs.get(i + 1).getName();
            paginator.addLine(line);
        }

        int page = args.getInteger(1, 1);
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
    }

    @Command(
            aliases = { "npc" },
            usage = "lookclose --(random|r)look [true|false] --(random|r)pitchrange [min,max] --(random|r)yawrange [min,max]",
            desc = "Toggle whether a NPC will look when a player is near",
            modifiers = { "lookclose", "look", "rotate" },
            min = 1,
            max = 1,
            permission = "citizens.npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean toggle = true;
        if (args.hasAnyValueFlag("randomlook", "rlook")) {
            boolean enableRandomLook = Boolean.parseBoolean(args.getFlag("randomlook", args.getFlag("rlook")));
            npc.getOrAddTrait(LookClose.class).setRandomLook(enableRandomLook);
            Messaging.sendTr(sender,
                    enableRandomLook ? Messages.LOOKCLOSE_RANDOM_SET : Messages.LOOKCLOSE_RANDOM_STOPPED,
                    npc.getName());
            toggle = false;
        }
        if (args.hasAnyValueFlag("randomlookdelay", "rlookdelay")) {
            int delay = Integer.parseInt(args.getFlag("randomlookdelay", args.getFlag("rlookdelay")));
            delay = Math.max(1, delay);
            npc.getOrAddTrait(LookClose.class).setRandomLookDelay(delay);
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_DELAY_SET, npc.getName(), delay);
            toggle = false;
        }
        if (args.hasAnyValueFlag("randompitchrange", "rpitchrange")) {
            String flag = args.getFlag("randompitchrange", args.getFlag("rpitchrange"));
            try {
                String[] parts = flag.split(",");
                float min = Float.parseFloat(parts[0]), max = Float.parseFloat(parts[1]);
                if (min > max)
                    throw new IllegalArgumentException();
                npc.getOrAddTrait(LookClose.class).setRandomLookPitchRange(min, max);
            } catch (Exception e) {
                throw new CommandException(Messaging.tr(Messages.ERROR_SETTING_LOOKCLOSE_RANGE, flag));
            }
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_PITCH_RANGE_SET, npc.getName(), flag);
            toggle = false;
        }
        if (args.hasAnyValueFlag("randomyawrange", "ryawrange")) {
            String flag = args.getFlag("randomyawrange", args.getFlag("ryawrange"));
            try {
                String[] parts = flag.split(",");
                float min = Float.parseFloat(parts[0]), max = Float.parseFloat(parts[1]);
                if (min > max)
                    throw new IllegalArgumentException();
                npc.getOrAddTrait(LookClose.class).setRandomLookYawRange(min, max);
            } catch (Exception e) {
                throw new CommandException(Messaging.tr(Messages.ERROR_SETTING_LOOKCLOSE_RANGE, flag));
            }
            Messaging.sendTr(sender, Messages.LOOKCLOSE_RANDOM_YAW_RANGE_SET, npc.getName(), flag);
            toggle = false;
        }
        if (toggle) {
            Messaging.sendTr(sender,
                    npc.getOrAddTrait(LookClose.class).toggle() ? Messages.LOOKCLOSE_SET : Messages.LOOKCLOSE_STOPPED,
                    npc.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "metadata set|get|remove [key] (value) (-t(emporary))",
            desc = "Manages NPC metadata",
            modifiers = { "metadata" },
            flags = "t",
            min = 2,
            max = 4,
            permission = "citizens.npc.metadata")
    @Requirements(selected = true, ownership = true)
    public void metadata(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String command = args.getString(1).toLowerCase();
        if (command.equals("set")) {
            if (args.argsLength() != 4)
                throw new CommandException();
            if (args.hasFlag('t')) {
                npc.data().set(args.getString(2), args.getString(3));
            } else {
                npc.data().setPersistent(args.getString(2), args.getString(3));
            }
            Messaging.sendTr(sender, Messages.METADATA_SET, args.getString(2), args.getString(3));
        } else if (command.equals("get")) {
            if (args.argsLength() != 3) {
                throw new CommandException();
            }
            Messaging.send(sender, npc.data().get(args.getString(2), "null"));
        } else if (command.equals("remove")) {
            if (args.argsLength() != 3) {
                throw new CommandException();
            }
            npc.data().remove(args.getString(2));
            Messaging.sendTr(sender, Messages.METADATA_UNSET, args.getString(2));
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
    public void minecart(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.hasValueFlag("item")) {
            String raw = args.getFlag("item");
            int data = 0;
            if (raw.contains(":")) {
                int dataIndex = raw.indexOf(':');
                data = Integer.parseInt(raw.substring(dataIndex + 1));
                raw = raw.substring(0, dataIndex);
            }
            Material material = Material.matchMaterial(raw);
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
    public void mount(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.hasValueFlag("onnpc")) {
            NPC mount;
            try {
                UUID uuid = UUID.fromString(args.getFlag("onnpc"));
                mount = CitizensAPI.getNPCRegistry().getByUniqueId(uuid);
            } catch (IllegalArgumentException ex) {
                mount = CitizensAPI.getNPCRegistry().getById(args.getFlagInteger("onnpc"));
            }
            if (mount == null || !mount.isSpawned()) {
                throw new CommandException(Messaging.tr(Messages.MOUNT_NPC_MUST_BE_SPAWNED, args.getFlag("onnpc")));
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
            modifiers = { "name" },
            usage = "name",
            desc = "Toggle nameplate visibility",
            min = 1,
            max = 1,
            flags = "h",
            permission = "citizens.npc.name")
    @Requirements(selected = true, ownership = true, livingEntity = true)
    public void name(CommandContext args, CommandSender sender, NPC npc) {
        String old = npc.data().<Object> get(NPC.NAMEPLATE_VISIBLE_METADATA, true).toString();
        if (args.hasFlag('h')) {
            old = "hover";
        } else {
            old = old.equals("hover") ? "true" : "" + !Boolean.parseBoolean(old);
        }
        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, old);
        Messaging.sendTr(sender, Messages.NAMEPLATE_VISIBILITY_TOGGLED);
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0, permission = "citizens.npc.info")
    public void npc(CommandContext args, CommandSender sender, final NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
        Messaging.send(sender, "    <a>Type: <e>" + npc.getOrAddTrait(MobType.class).getType());
        if (npc.isSpawned()) {
            Location loc = npc.getEntity().getLocation();
            String format = "    <a>Spawned at <e>%d, %d, %d <a>in world<e> %s";
            Messaging.send(sender,
                    String.format(format, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
        }
        Messaging.send(sender, "    <a>Traits<e>");
        for (Trait trait : npc.getTraits()) {
            if (CitizensAPI.getTraitFactory().isInternalTrait(trait))
                continue;
            String message = "     <e>- <a>" + trait.getName();
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
    public void ocelot(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        OcelotModifiers trait = npc.getOrAddTrait(OcelotModifiers.class);
        if (args.hasFlag('s')) {
            trait.setSitting(true);
        } else if (args.hasFlag('n')) {
            trait.setSitting(false);
        }
        if (args.hasValueFlag("type")) {
            Ocelot.Type type = Util.matchEnum(Ocelot.Type.values(), args.getFlag("type"));
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
            usage = "owner [name]",
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
        String name = args.getString(1);
        if (ownerTrait.isOwnedBy(name))
            throw new CommandException(Messages.ALREADY_OWNER, name, npc.getName());
        ownerTrait.setOwner(name);
        boolean serverOwner = name.equalsIgnoreCase(Owner.SERVER);
        Messaging.sendTr(sender, serverOwner ? Messages.OWNER_SET_SERVER : Messages.OWNER_SET, npc.getName(), name);
    }

    @Command(
            aliases = { "npc" },
            usage = "passive (--set [true|false])",
            desc = "Sets whether an NPC damages other entities or not",
            modifiers = { "passive" },
            min = 1,
            max = 1,
            permission = "citizens.npc.passive")
    public void passive(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean passive = args.hasValueFlag("set") ? Boolean.parseBoolean(args.getFlag("set"))
                : npc.data().get(NPC.DAMAGE_OTHERS_METADATA, true);
        npc.data().setPersistent(NPC.DAMAGE_OTHERS_METADATA, !passive);
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
    public void pathfindingOptions(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String output = "";
        if (args.hasValueFlag("avoid-water") || args.hasValueFlag("aw")) {
            String raw = args.getFlag("avoid-water", args.getFlag("aw"));
            boolean avoid = Boolean.parseBoolean(raw);
            npc.getNavigator().getDefaultParameters().avoidWater(avoid);
            output += Messaging.tr(avoid ? Messages.PATHFINDING_OPTIONS_AVOID_WATER_SET
                    : Messages.PATHFINDING_OPTIONS_AVOID_WATER_UNSET, npc.getName());
        }
        if (args.hasValueFlag("stationary-ticks")) {
            int ticks = Integer.parseInt(args.getFlag("stationary-ticks"));
            if (ticks < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().stationaryTicks(ticks);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_STATIONARY_TICKS_SET, npc.getName(), ticks);
        }
        if (args.hasValueFlag("distance-margin")) {
            double distance = Double.parseDouble(args.getFlag("distance-margin"));
            if (distance < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().distanceMargin(Math.pow(distance, 2));
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_DISTANCE_MARGIN_SET, npc.getName(), distance);

        }
        if (args.hasValueFlag("path-distance-margin")) {
            double distance = Double.parseDouble(args.getFlag("path-distance-margin"));
            if (distance < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().pathDistanceMargin(Math.pow(distance, 2));
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_PATH_DISTANCE_MARGIN_SET, npc.getName(), distance);
        }
        if (args.hasValueFlag("attack-range")) {
            double range = Double.parseDouble(args.getFlag("attack-range"));
            if (range < 0)
                throw new CommandUsageException();
            npc.getNavigator().getDefaultParameters().attackRange(Math.pow(range, 2));
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_ATTACK_RANGE_SET, npc.getName(), range);
        }
        if (args.hasValueFlag("use-new-finder")) {
            String raw = args.getFlag("use-new-finder", args.getFlag("unf"));
            boolean use = Boolean.parseBoolean(raw);
            npc.getNavigator().getDefaultParameters().useNewPathfinder(use);
            output += Messaging.tr(Messages.PATHFINDING_OPTIONS_USE_NEW_FINDER, npc.getName(), use);
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
            usage = "pathto [x] [y] [z]",
            desc = "Starts pathfinding to a certain location",
            modifiers = { "pathto" },
            min = 4,
            max = 4,
            permission = "citizens.npc.pathto")
    public void pathto(CommandContext args, CommandSender sender, NPC npc) {
        Location loc = npc.getStoredLocation();
        loc.setX(args.getDouble(1));
        loc.setY(args.getDouble(2));
        loc.setZ(args.getDouble(3));
        npc.getNavigator().setTarget(loc);
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
        boolean remove = !npc.data().get("removefromplayerlist", Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
        if (args.hasFlag('a')) {
            remove = false;
        } else if (args.hasFlag('r')) {
            remove = true;
        }
        npc.data().setPersistent("removefromplayerlist", remove);
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
            usage = "pose (--save [name] (-d)|--assume [name]|--remove [name]|--default [name]) (-a)",
            desc = "Changes/Saves/Lists NPC's head pose(s)",
            flags = "ad",
            modifiers = { "pose" },
            min = 1,
            max = 2,
            permission = "citizens.npc.pose")
    public void pose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Poses trait = npc.getOrAddTrait(Poses.class);
        if (args.hasValueFlag("save")) {
            if (args.getFlag("save").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(args.getFlag("save"), args.getSenderLocation(), args.hasFlag('d'))) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, args.getFlag("save"));
        } else if (args.hasValueFlag("default")) {
            String pose = args.getFlag("default");
            if (!trait.hasPose(pose))
                throw new CommandException(Messages.POSE_MISSING, pose);
            trait.setDefaultPose(pose);
            Messaging.sendTr(sender, Messages.DEFAULT_POSE_SET, pose);
        } else if (args.hasValueFlag("assume")) {
            String pose = args.getFlag("assume");
            if (pose.isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (!trait.hasPose(pose))
                throw new CommandException(Messages.POSE_MISSING, pose);
            trait.assumePose(pose);
        } else if (args.hasValueFlag("remove")) {
            if (args.getFlag("remove").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);
            if (trait.removePose(args.getFlag("remove"))) {
                Messaging.sendTr(sender, Messages.POSE_REMOVED);
            } else
                throw new CommandException(Messages.POSE_MISSING, args.getFlag("remove"));
        } else if (!args.hasFlag('a')) {
            trait.describe(sender, args.getInteger(1, 1));
        }

        if (!args.hasFlag('a'))
            return;
        if (args.getSenderLocation() == null)
            throw new ServerCommandException();
        Location location = args.getSenderLocation();
        trait.assumePose(location);
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
    public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        EntityType type = npc.getOrAddTrait(MobType.class).getType();
        if (type != EntityType.VILLAGER && !type.name().equals("ZOMBIE_VILLAGER")) {
            throw new RequirementMissingException(Messaging.tr(CommandMessages.REQUIREMENTS_INVALID_MOB_TYPE,
                    type.name().toLowerCase().replace('_', ' ')));
        }
        String profession = args.getString(1);
        Profession parsed = Util.matchEnum(Profession.values(), profession.toUpperCase());
        if (parsed == null) {
            throw new CommandException(Messages.INVALID_PROFESSION, args.getString(1),
                    Joiner.on(',').join(Profession.values()));
        }
        npc.getOrAddTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.sendTr(sender, Messages.PROFESSION_SET, npc.getName(), profession);
    }

    @Command(
            aliases = { "npc" },
            usage = "rabbittype [type]",
            desc = "Set the Type of a Rabbit NPC",
            modifiers = { "rabbittype", "rbtype" },
            min = 2,
            permission = "citizens.npc.rabbittype")
    @Requirements(selected = true, ownership = true, types = { EntityType.RABBIT })
    public void rabbitType(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Rabbit.Type type;
        try {
            type = Rabbit.Type.valueOf(args.getString(1).toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CommandException(Messages.INVALID_RABBIT_TYPE, Joiner.on(',').join(Rabbit.Type.values()));
        }
        npc.getOrAddTrait(RabbitType.class).setType(type);
        Messaging.sendTr(sender, Messages.RABBIT_TYPE_SET, npc.getName(), type.name());
    }

    @Command(
            aliases = { "npc" },
            usage = "remove|rem (all|id|name| --owner [owner] | --eid [entity uuid])",
            desc = "Remove a NPC",
            modifiers = { "remove", "rem" },
            min = 1,
            max = 2)
    @Requirements
    public void remove(final CommandContext args, final CommandSender sender, NPC npc) throws CommandException {
        if (args.hasValueFlag("owner")) {
            String owner = args.getFlag("owner");
            Collection<NPC> npcs = Lists.newArrayList(CitizensAPI.getNPCRegistry());
            for (NPC o : npcs) {
                if (o.getOrAddTrait(Owner.class).isOwnedBy(owner)) {
                    o.destroy(sender);
                }
            }
            Messaging.sendTr(sender, Messages.NPCS_REMOVED);
            return;
        }
        if (args.hasValueFlag("eid")) {
            Entity entity = Bukkit.getServer().getEntity(UUID.fromString(args.getFlag("eid")));
            if (entity != null && (npc = CitizensAPI.getNPCRegistry().getNPC(entity)) != null) {
                npc.destroy(sender);
                Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName());
                return;
            } else {
                Messaging.sendErrorTr(sender, Messages.NPC_NOT_FOUND);
                return;
            }
        }
        if (args.argsLength() == 2) {
            if (args.getString(1).equalsIgnoreCase("all")) {
                if (!sender.hasPermission("citizens.admin.remove.all") && !sender.hasPermission("citizens.admin"))
                    throw new NoPermissionsException();
                CitizensAPI.getNPCRegistry().deregisterAll();
                Messaging.sendTr(sender, Messages.REMOVED_ALL_NPCS);
                return;
            } else {
                NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
                    @Override
                    public void run(NPC npc) throws CommandException {
                        if (npc == null)
                            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
                        if (!(sender instanceof ConsoleCommandSender)
                                && !npc.getOrAddTrait(Owner.class).isOwnedBy(sender))
                            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
                        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
                            throw new NoPermissionsException();
                        npc.destroy(sender);
                        Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName());
                    }
                };
                NPCCommandSelector.startWithCallback(callback, CitizensAPI.getNPCRegistry(), sender, args,
                        args.getString(1));
                return;
            }
        }
        if (npc == null)
            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
        if (!(sender instanceof ConsoleCommandSender) && !npc.getOrAddTrait(Owner.class).isOwnedBy(sender))
            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.destroy(sender);
        Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName());
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
        String newName = Colorizer.parseColors(args.getJoinedStrings(1));
        int nameLength = SpigotUtil.getMaxNameLength(npc.getOrAddTrait(MobType.class).getType());
        if (newName.length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG, nameLength);
            newName = newName.substring(0, nameLength);
        }
        npc.setName(newName);

        Messaging.sendTr(sender, Messages.NPC_RENAMED, oldName, newName);
    }

    @Command(
            aliases = { "npc" },
            usage = "respawn [delay in ticks]",
            desc = "Sets an NPC's respawn delay in ticks",
            modifiers = { "respawn" },
            min = 1,
            max = 2,
            permission = "citizens.npc.respawn")
    public void respawn(CommandContext args, CommandSender sender, NPC npc) {
        if (args.argsLength() > 1) {
            int delay = args.getInteger(1);
            npc.data().setPersistent(NPC.RESPAWN_DELAY_METADATA, delay);
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_SET, delay);
        } else {
            Messaging.sendTr(sender, Messages.RESPAWN_DELAY_DESCRIBE, npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1));
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
    public void scoreboard(CommandContext args, CommandSender sender, NPC npc) {
        ScoreboardTrait trait = npc.getOrAddTrait(ScoreboardTrait.class);
        String output = "";
        if (args.hasValueFlag("addtag")) {
            for (String tag : args.getFlag("addtag").split(",")) {
                trait.addTag(tag);
            }
            output += " " + Messaging.tr(Messages.ADDED_SCOREBOARD_TAGS, args.getFlag("addtag"));
        }
        if (args.hasValueFlag("removetag")) {
            for (String tag : args.getFlag("removetag").split(",")) {
                trait.removeTag(tag);
            }
            output += " " + Messaging.tr(Messages.REMOVED_SCOREBOARD_TAGS, args.getFlag("removetag"));
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
    public void script(CommandContext args, CommandSender sender, NPC npc) {
        ScriptTrait trait = npc.getOrAddTrait(ScriptTrait.class);
        if (args.hasValueFlag("add")) {
            List<String> files = new ArrayList<String>();
            for (String file : args.getFlag("add").split(",")) {
                if (!trait.validateFile(file)) {
                    Messaging.sendErrorTr(sender, Messages.INVALID_SCRIPT_FILE, file);
                    return;
                }
                files.add(file);
            }
            trait.addScripts(files);
        }
        if (args.hasValueFlag("remove")) {
            trait.removeScripts(Arrays.asList(args.getFlag("remove").split(",")));
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
    public void select(CommandContext args, final CommandSender sender, final NPC npc) throws CommandException {
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
        NPCRegistry registry = args.hasValueFlag("registry") ? CitizensAPI.getNamedNPCRegistry(args.getFlag("registry"))
                : CitizensAPI.getNPCRegistry();
        if (args.argsLength() <= 1) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            double range = Math.abs(args.getFlagDouble("r", 10));
            Entity player = (Player) sender;
            final Location location = args.getSenderLocation();
            List<Entity> search = player.getNearbyEntities(range, range, range);
            Collections.sort(search, new Comparator<Entity>() {
                @Override
                public int compare(Entity o1, Entity o2) {
                    double d = o1.getLocation().distanceSquared(location) - o2.getLocation().distanceSquared(location);
                    return d > 0 ? 1 : d < 0 ? -1 : 0;
                }
            });
            for (Entity possibleNPC : search) {
                NPC test = registry.getNPC(possibleNPC);
                if (test == null)
                    continue;
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
    public void sheep(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SheepTrait trait = npc.getOrAddTrait(SheepTrait.class);
        boolean hasArg = false;
        if (args.hasValueFlag("sheared")) {
            trait.setSheared(Boolean.valueOf(args.getFlag("sheared")));
            hasArg = true;
        }
        if (args.hasValueFlag("color")) {
            DyeColor color = Util.matchEnum(DyeColor.values(), args.getFlag("color"));
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
            usage = "skin (-c -l(atest)) [name] (or --url [url] or -t [uuid/name] [data] [signature])",
            desc = "Sets an NPC's skin name. Use -l to set the skin to always update to the latest",
            modifiers = { "skin" },
            min = 1,
            max = 4,
            flags = "ctl",
            permission = "citizens.npc.skin")
    @Requirements(types = EntityType.PLAYER, selected = true, ownership = true)
    public void skin(final CommandContext args, final CommandSender sender, final NPC npc) throws CommandException {
        String skinName = npc.getName();
        final SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);
        if (args.hasFlag('c')) {
            trait.clearTexture();
        } else if (args.hasValueFlag("url")) {
            final String url = args.getFlag("url");
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
                                trait.setSkinPersistent(uuid, signature, textureEncoded);
                                Messaging.sendTr(sender, Messages.SKIN_URL_SET, npc.getName(), url);
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
    public void skinLayers(final CommandContext args, final CommandSender sender, final NPC npc)
            throws CommandException {
        SkinLayers trait = npc.getOrAddTrait(SkinLayers.class);
        if (args.hasValueFlag("cape")) {
            trait.setVisible(Layer.CAPE, Boolean.valueOf(args.getFlag("cape")));
        }
        if (args.hasValueFlag("hat")) {
            trait.setVisible(Layer.HAT, Boolean.valueOf(args.getFlag("hat")));
        }
        if (args.hasValueFlag("jacket")) {
            trait.setVisible(Layer.JACKET, Boolean.valueOf(args.getFlag("jacket")));
        }
        if (args.hasValueFlag("sleeves")) {
            boolean hasSleeves = Boolean.valueOf(args.getFlag("sleeves"));
            trait.setVisible(Layer.LEFT_SLEEVE, hasSleeves);
            trait.setVisible(Layer.RIGHT_SLEEVE, hasSleeves);
        }
        if (args.hasValueFlag("pants")) {
            boolean hasPants = Boolean.valueOf(args.getFlag("pants"));
            trait.setVisible(Layer.LEFT_PANTS, hasPants);
            trait.setVisible(Layer.RIGHT_PANTS, hasPants);
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
    @Requirements(selected = true, ownership = true, livingEntity = true, excludedTypes = { EntityType.PLAYER })
    public void sound(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
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
            if (args.hasValueFlag("death")) {
                deathSound = args.getFlag("death").equals("d") ? null : NMS.getSound(args.getFlag("death"));
            }
            if (args.hasValueFlag("ambient")) {
                ambientSound = args.getFlag("ambient").equals("d") ? null : NMS.getSound(args.getFlag("ambient"));
            }
            if (args.hasValueFlag("hurt")) {
                hurtSound = args.getFlag("hurt").equals("d") ? null : NMS.getSound(args.getFlag("hurt"));
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
        if ((!StringUtils.isNullOrEmpty(ambientSound) && !ambientSound.equals("none"))
                || (!StringUtils.isNullOrEmpty(deathSound) && !deathSound.equals("none"))
                || (!StringUtils.isNullOrEmpty(hurtSound) && !hurtSound.equals("none"))) {
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
            usage = "speak message to speak --target npcid|player_name --type vocal_type",
            desc = "Uses the NPCs SpeechController to talk",
            modifiers = { "speak" },
            min = 2,
            permission = "citizens.npc.speak")
    public void speak(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String type = npc.getOrAddTrait(Speech.class).getDefaultVocalChord();
        String message = Colorizer.parseColors(args.getJoinedStrings(1));

        if (message.length() <= 0) {
            Messaging.send(sender, "Default Vocal Chord for " + npc.getName() + ": "
                    + npc.getOrAddTrait(Speech.class).getDefaultVocalChord());
            return;
        }

        SpeechContext context = new SpeechContext(message);

        if (args.hasValueFlag("target")) {
            if (args.getFlag("target").matches("\\d+")) {
                NPC target = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(args.getFlag("target")));
                if (target != null)
                    context.addRecipient(target.getEntity());
            } else {
                Player player = Bukkit.getPlayerExact(args.getFlag("target"));
                if (player != null) {
                    context.addRecipient((Entity) player);
                }
            }
        }

        if (args.hasValueFlag("type")) {
            if (CitizensAPI.getSpeechFactory().isRegistered(args.getFlag("type")))
                type = args.getFlag("type");
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
    public void swim(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean swim = args.hasValueFlag("set") ? Boolean.parseBoolean(args.getFlag("set"))
                : !npc.data().get(NPC.SWIMMING_METADATA, true);
        npc.data().setPersistent(NPC.SWIMMING_METADATA, swim);
        Messaging.sendTr(sender, swim ? Messages.SWIMMING_SET : Messages.SWIMMING_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "targetable",
            desc = "Toggles an NPC's targetability",
            modifiers = { "targetable" },
            min = 1,
            max = 1,
            permission = "citizens.npc.targetable")
    public void targetable(CommandContext args, CommandSender sender, NPC npc) {
        boolean targetable = !npc.data().get(NPC.TARGETABLE_METADATA,
                npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        if (args.hasFlag('t')) {
            npc.data().set(NPC.TARGETABLE_METADATA, targetable);
        } else {
            npc.data().setPersistent(NPC.TARGETABLE_METADATA, targetable);
        }
        Messaging.sendTr(sender, targetable ? Messages.TARGETABLE_SET : Messages.TARGETABLE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "tp",
            desc = "Teleport to a NPC",
            modifiers = { "tp", "teleport" },
            min = 1,
            max = 1,
            permission = "citizens.npc.tp")
    public void tp(CommandContext args, Player player, NPC npc) {
        Location to = npc.getOrAddTrait(CurrentLocation.class).getLocation();
        if (to == null) {
            Messaging.sendError(player, Messages.TELEPORT_NPC_LOCATION_NOT_FOUND);
            return;
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
            usage = "type [type]",
            desc = "Sets an NPC's entity type",
            modifiers = { "type" },
            min = 2,
            max = 2,
            permission = "citizens.npc.type")
    public void type(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        EntityType type = Util.matchEntityType(args.getString(1));
        if (type == null)
            throw new CommandException(Messages.INVALID_ENTITY_TYPE, args.getString(1));
        npc.setBukkitEntityType(type);
        Messaging.sendTr(sender, Messages.ENTITY_TYPE_SET, npc.getName(), args.getString(1));
    }

    @Command(
            aliases = { "npc" },
            usage = "vulnerable (-t)",
            desc = "Toggles an NPC's vulnerability",
            modifiers = { "vulnerable" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "citizens.npc.vulnerable")
    public void vulnerable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
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
            usage = "wither (--charged [charged])",
            desc = "Sets wither modifiers",
            modifiers = { "wither" },
            min = 1,
            requiresFlags = true,
            max = 1,
            permission = "citizens.npc.wither")
    @Requirements(selected = true, ownership = true, types = { EntityType.WITHER })
    public void wither(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        WitherTrait trait = npc.getOrAddTrait(WitherTrait.class);
        if (args.hasValueFlag("charged")) {
            trait.setCharged(Boolean.valueOf(args.getFlag("charged")));
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
    public void wolf(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
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
        if (args.hasValueFlag("collar")) {
            String unparsed = args.getFlag("collar");
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
