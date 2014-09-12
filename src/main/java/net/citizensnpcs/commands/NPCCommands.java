package net.citizensnpcs.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.NoPermissionsException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.npc.entity.nonliving.FallingBlockController.FallingBlockNPC;
import net.citizensnpcs.npc.entity.nonliving.ItemController.ItemNPC;
import net.citizensnpcs.npc.entity.nonliving.ItemFrameController.ItemFrameNPC;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.NPCSkeletonType;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.ZombieModifier;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final NPCRegistry npcRegistry;
    private final NPCSelector selector;

    public NPCCommands(Citizens plugin) {
        npcRegistry = CitizensAPI.getNPCRegistry();
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
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Ageable))
            throw new CommandException(Messages.MOBTYPE_CANNOT_BE_AGED);
        Age trait = npc.getTrait(Age.class);

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
            usage = "anchor (--save [name]|--assume [name]|--remove [name]) (-a)(-c)",
            desc = "Changes/Saves/Lists NPC's location anchor(s)",
            flags = "ac",
            modifiers = { "anchor" },
            min = 1,
            max = 3,
            permission = "citizens.npc.anchor")
    public void anchor(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Anchors trait = npc.getTrait(Anchors.class);
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
            Paginator paginator = new Paginator().header("Anchors");
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
            usage = "controllable|control (-m,-y,-n)",
            desc = "Toggles whether the NPC can be ridden and controlled",
            modifiers = { "controllable", "control" },
            min = 1,
            max = 1,
            flags = "myn")
    public void controllable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if ((npc.isSpawned() && !sender.hasPermission("citizens.npc.controllable."
                + npc.getEntity().getType().name().toLowerCase().replace("_", "")))
                || !sender.hasPermission("citizens.npc.controllable"))
            throw new NoPermissionsException();
        if (!npc.hasTrait(Controllable.class)) {
            npc.addTrait(new Controllable(false));
        }
        Controllable trait = npc.getTrait(Controllable.class);
        boolean enabled = trait.toggle();
        if (args.hasFlag('y')) {
            enabled = trait.setEnabled(true);
        } else if (args.hasFlag('n')) {
            enabled = trait.setEnabled(false);
        }
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
            copy.getTrait(CurrentLocation.class).setLocation(location);
        }

        CommandSenderCreateNPCEvent event = sender instanceof Player ? new PlayerCreateNPCEvent((Player) sender, copy)
        : new CommandSenderCreateNPCEvent(sender, copy);
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
            usage = "create [name] ((-b,u) --at (x:y:z:world) --type (type) --trait ('trait1, trait2...') --b (behaviours))",
            desc = "Create a new NPC",
            flags = "bu",
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

        int nameLength = type == EntityType.PLAYER ? 16 : 64;
        if (name.length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG);
            name = name.substring(0, nameLength);
        }
        if (name.length() == 0)
            throw new CommandException();

        if (!sender.hasPermission("citizens.npc.create.*") && !sender.hasPermission("citizens.npc.createall")
                && !sender.hasPermission("citizens.npc.create." + type.name().toLowerCase().replace("_", "")))
            throw new NoPermissionsException();

        npc = npcRegistry.createNPC(type, name);
        String msg = "You created [[" + npc.getName() + "]]";

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

        // Initialize necessary traits
        if (!Setting.SERVER_OWNS_NPCS.asBoolean()) {
            npc.getTrait(Owner.class).setOwner(sender);
        }
        npc.getTrait(MobType.class).setType(type);

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
        }

        if (spawnLoc == null) {
            npc.destroy();
            throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
        }

        if (!args.hasFlag('u')) {
            npc.spawn(spawnLoc);
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
            npc.getTrait(Age.class).setAge(age);
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
                npc.getTrait(Spawned.class).setSpawned(false);
                npc.despawn(DespawnReason.REMOVAL);
                Messaging.sendTr(sender, Messages.NPC_DESPAWNED, npc.getName());
            }
        };
        if (npc == null || args.argsLength() == 2) {
            if (args.argsLength() < 2) {
                throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
            }
            NPCCommandSelector.startWithCallback(callback, npcRegistry, sender, args, args.getString(1));
        } else {
            callback.run(npc);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "flyable (true|false)",
            desc = "Toggles or sets an NPC's flyable status",
            modifiers = { "flyable" },
            min = 1,
            max = 2,
            permission = "citizens.npc.flyable")
    @Requirements(selected = true, ownership = true, excludedTypes = { EntityType.BAT, EntityType.BLAZE,
            EntityType.ENDER_DRAGON, EntityType.GHAST, EntityType.WITHER })
    public void flyable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean flyable = args.argsLength() == 2 ? args.getString(1).equals("true") : !npc.isFlyable();
        npc.setFlyable(flyable);
        flyable = npc.isFlyable(); // may not have applied, eg bats always
        // flyable
        Messaging.sendTr(sender, flyable ? Messages.FLYABLE_SET : Messages.FLYABLE_UNSET);
    }

    @Command(
            aliases = { "npc" },
            usage = "gamemode [gamemode]",
            desc = "Changes the gamemode",
            modifiers = { "gamemode" },
            min = 1,
            max = 2,
            permission = "citizens.npc.gravity")
    @Requirements(selected = true, ownership = true, types = { EntityType.PLAYER })
    public void gamemode(CommandContext args, CommandSender sender, NPC npc) {
        Player player = (Player) npc.getEntity();
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.GAMEMODE_DESCRIBE, npc.getName(), player.getGameMode().name()
                    .toLowerCase());
            return;
        }
        GameMode mode = null;
        try {
            int value = args.getInteger(1);
            mode = GameMode.getByValue(value);
        } catch (NumberFormatException ex) {
            try {
                mode = GameMode.valueOf(args.getString(1));
            } catch (IllegalArgumentException e) {
            }
        }
        if (mode == null) {
            Messaging.sendErrorTr(sender, Messages.GAMEMODE_INVALID, args.getString(1));
            return;
        }
        player.setGameMode(mode);
        Messaging.sendTr(sender, Messages.GAMEMODE_SET, mode.name().toLowerCase());
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
        boolean enabled = npc.getTrait(Gravity.class).toggle();
        String key = !enabled ? Messages.GRAVITY_ENABLED : Messages.GRAVITY_DISABLED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "horse (--color color) (--type type) (--style style) (-cb)",
            desc = "Sets horse modifiers",
            help = "Use the -c flag to make the horse have a chest, or the -b flag to stop them from having a chest.",
            modifiers = { "horse" },
            min = 1,
            max = 1,
            flags = "cb",
            permission = "citizens.npc.horse")
    @Requirements(selected = true, ownership = true, types = { EntityType.HORSE })
    public void horse(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        HorseModifiers horse = npc.getTrait(HorseModifiers.class);
        String output = "";
        if (args.hasFlag('c')) {
            horse.setCarryingChest(true);
            output += Messaging.tr(Messages.HORSE_CHEST_SET) + " ";
        } else if (args.hasFlag('b')) {
            horse.setCarryingChest(false);
            output += Messaging.tr(Messages.HORSE_CHEST_UNSET) + " ";
        }
        if (args.hasValueFlag("color") || args.hasValueFlag("colour")) {
            String colorRaw = args.getFlag("color", args.getFlag("colour"));
            Color color = Util.matchEnum(Color.values(), colorRaw);
            if (color == null) {
                String valid = Util.listValuesPretty(Color.values());
                throw new CommandException(Messages.INVALID_HORSE_COLOR, valid);
            }
            horse.setColor(color);
            output += Messaging.tr(Messages.HORSE_COLOR_SET, Util.prettyEnum(color));
        }
        if (args.hasValueFlag("type")) {
            Variant variant = Util.matchEnum(Variant.values(), args.getFlag("type"));
            if (variant == null) {
                String valid = Util.listValuesPretty(Variant.values());
                throw new CommandException(Messages.INVALID_HORSE_VARIANT, valid);
            }
            horse.setType(variant);
            output += Messaging.tr(Messages.HORSE_TYPE_SET, Util.prettyEnum(variant));
        }
        if (args.hasValueFlag("style")) {
            Style style = Util.matchEnum(Style.values(), args.getFlag("style"));
            if (style == null) {
                String valid = Util.listValuesPretty(Style.values());
                throw new CommandException(Messages.INVALID_HORSE_STYLE, valid);
            }
            horse.setStyle(style);
            output += Messaging.tr(Messages.HORSE_STYLE_SET, Util.prettyEnum(style));
        }
        if (output.isEmpty()) {
            Messaging.sendTr(sender, Messages.HORSE_DESCRIBE, Util.prettyEnum(horse.getColor()),
                    Util.prettyEnum(horse.getType()), Util.prettyEnum(horse.getStyle()));
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
            usage = "item [item] (data)",
            desc = "Sets the NPC's item",
            modifiers = { "item", },
            min = 2,
            max = 3,
            flags = "",
            permission = "citizens.npc.item")
    @Requirements(selected = true, ownership = true, types = { EntityType.DROPPED_ITEM, EntityType.ITEM_FRAME,
            EntityType.FALLING_BLOCK })
    public void item(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Material mat = Material.matchMaterial(args.getString(1));
        if (mat == null)
            throw new CommandException(Messages.UNKNOWN_MATERIAL);
        int data = args.getInteger(2, 0);
        switch (npc.getEntity().getType()) {
            case DROPPED_ITEM:
                ((org.bukkit.entity.Item) npc.getEntity()).getItemStack().setType(mat);
                ((ItemNPC) npc.getEntity()).setType(mat, data);
                break;
            case ITEM_FRAME:
                ((ItemFrame) npc.getEntity()).getItem().setType(mat);
                ((ItemFrameNPC) npc.getEntity()).setType(mat, data);
                break;
            case FALLING_BLOCK:
                ((FallingBlockNPC) npc.getEntity()).setType(mat, data);
                break;
            default:
                break;
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
                : npcRegistry;
        if (source == null)
            throw new CommandException();
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : source.sorted()) {
                npcs.add(add);
            }
        } else if (args.getValueFlags().size() == 0 && sender instanceof Player) {
            for (NPC add : source.sorted()) {
                if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(sender)) {
                    npcs.add(add);
                }
            }
        } else {
            if (args.hasValueFlag("owner")) {
                String name = args.getFlag("owner");
                for (NPC add : source.sorted()) {
                    if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(name)) {
                        npcs.add(add);
                    }
                }
            }

            if (args.hasValueFlag("type")) {
                EntityType type = Util.matchEntityType(args.getFlag("type"));

                if (type == null)
                    throw new CommandException(Messages.COMMAND_INVALID_MOBTYPE, type);

                for (NPC add : source) {
                    if (!npcs.contains(add) && add.getTrait(MobType.class).getType() == type)
                        npcs.add(add);
                }
            }
        }

        Paginator paginator = new Paginator().header("NPCs");
        paginator.addLine("<e>Key: <a>ID  <b>Name");
        for (int i = 0; i < npcs.size(); i += 2) {
            String line = "<a>" + npcs.get(i).getId() + "<b>  " + npcs.get(i).getName();
            if (npcs.size() >= i + 2)
                line += "      " + "<a>" + npcs.get(i + 1).getId() + "<b>  " + npcs.get(i + 1).getName();
            paginator.addLine(line);
        }

        int page = args.getInteger(1, 1);
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
    }

    @Command(
            aliases = { "npc" },
            usage = "lookclose",
            desc = "Toggle whether a NPC will look when a player is near",
            modifiers = { "lookclose", "look", "rotate" },
            min = 1,
            max = 1,
            permission = "citizens.npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, npc.getTrait(LookClose.class).toggle() ? Messages.LOOKCLOSE_SET
                : Messages.LOOKCLOSE_STOPPED, npc.getName());
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
    @Requirements(selected = true, ownership = true, types = { EntityType.MINECART, EntityType.MINECART_CHEST,
            EntityType.MINECART_COMMAND, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER, EntityType.MINECART_TNT })
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
            usage = "mount",
            desc = "Mounts a controllable NPC",
            modifiers = { "mount" },
            min = 1,
            max = 1,
            permission = "citizens.npc.controllable")
    public void mount(CommandContext args, Player player, NPC npc) {
        boolean enabled = npc.hasTrait(Controllable.class) && npc.getTrait(Controllable.class).isEnabled();
        if (!enabled) {
            Messaging.sendTr(player, Messages.NPC_NOT_CONTROLLABLE, npc.getName());
            return;
        }
        boolean success = npc.getTrait(Controllable.class).mount(player);
        if (!success)
            Messaging.sendTr(player, Messages.FAILED_TO_MOUNT_NPC, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "moveto x:y:z:world | x y z world",
            desc = "Teleports a NPC to a given location",
            modifiers = "moveto",
            min = 1,
            permission = "citizens.npc.moveto")
    public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned()) {
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
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
        Messaging.sendTr(sender, Messages.MOVETO_TELEPORTED, npc.getName(), to);
    }

    @Command(
            aliases = { "npc" },
            modifiers = { "name" },
            usage = "name",
            desc = "Toggle nameplate visibility",
            min = 1,
            max = 1,
            permission = "citizens.npc.name")
    @Requirements(selected = true, ownership = true, livingEntity = true)
    public void name(CommandContext args, CommandSender sender, NPC npc) {
        LivingEntity entity = (LivingEntity) npc.getEntity();
        entity.setCustomNameVisible(!entity.isCustomNameVisible());
        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, entity.isCustomNameVisible());
        Messaging.sendTr(sender, Messages.NAMEPLATE_VISIBILITY_TOGGLED);
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0, permission = "citizens.npc.info")
    public void npc(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
        Messaging.send(sender, "    <a>Type: <e>" + npc.getTrait(MobType.class).getType());
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
            flags = "sn",
            permission = "citizens.npc.ocelot")
    @Requirements(selected = true, ownership = true, types = { EntityType.OCELOT })
    public void ocelot(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        OcelotModifiers trait = npc.getTrait(OcelotModifiers.class);
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
        Owner ownerTrait = npc.getTrait(Owner.class);
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
        boolean passive = args.hasValueFlag("set") ? Boolean.parseBoolean(args.getFlag("set")) : npc.data().get(
                NPC.DAMAGE_OTHERS_METADATA, true);
        npc.data().setPersistent(NPC.DAMAGE_OTHERS_METADATA, !passive);
        Messaging.sendTr(sender, passive ? Messages.PASSIVE_SET : Messages.PASSIVE_UNSET, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "pathopt --avoid-water|aw [true|false]",
            desc = "Sets an NPC's pathfinding options",
            modifiers = { "pathopt", "po", "patho" },
            min = 1,
            max = 1,
            permission = "citizens.npc.pathfindingoptions")
    public void pathfindingOptions(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.hasValueFlag("avoid-water") || args.hasValueFlag("aw")) {
            String raw = args.getFlag("avoid-water", args.getFlag("aw"));
            boolean avoid = Boolean.parseBoolean(raw);
            npc.getNavigator().getDefaultParameters().avoidWater(avoid);
            Messaging.sendTr(sender, avoid ? Messages.PATHFINDING_OPTIONS_AVOID_WATER_SET
                    : Messages.PATHFINDING_OPTIONS_AVOID_WATER_UNSET, npc.getName());
        } else {
            throw new CommandException();
        }
    }

    @Command(aliases = { "npc" }, usage = "pathrange [range]", desc = "Sets an NPC's pathfinding range", modifiers = {
            "pathrange", "pathfindingrange", "prange" }, min = 2, max = 2, permission = "citizens.npc.pathfindingrange")
    public void pathfindingRange(CommandContext args, CommandSender sender, NPC npc) {
        double range = Math.max(1, args.getDouble(1));
        npc.getNavigator().getDefaultParameters().range((float) range);
        Messaging.sendTr(sender, Messages.PATHFINDING_RANGE_SET, range);
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
        } else if (args.hasFlag('r'))
            remove = true;
        npc.data().setPersistent("removefromplayerlist", remove);
        if (npc.isSpawned()) {
            NMS.addOrRemoveFromPlayerList(npc.getEntity(), remove);
        }
        Messaging.sendTr(sender, remove ? Messages.REMOVED_FROM_PLAYERLIST : Messages.ADDED_TO_PLAYERLIST,
                npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "pose (--save [name]|--assume [name]|--remove [name]) (-a)",
            desc = "Changes/Saves/Lists NPC's head pose(s)",
            flags = "a",
            modifiers = { "pose" },
            min = 1,
            max = 2,
            permission = "citizens.npc.pose")
    public void pose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Poses trait = npc.getTrait(Poses.class);
        if (args.hasValueFlag("save")) {
            if (args.getFlag("save").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (args.getSenderLocation() == null)
                throw new ServerCommandException();

            if (trait.addPose(args.getFlag("save"), args.getSenderLocation())) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, args.getFlag("save"));
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

        // Assume Player's pose
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
        Messaging
        .sendTr(sender, npc.getTrait(Powered.class).toggle() ? Messages.POWERED_SET : Messages.POWERED_STOPPED);
    }

    @Command(
            aliases = { "npc" },
            usage = "profession|prof [profession]",
            desc = "Set a NPC's profession",
            modifiers = { "profession", "prof" },
            min = 2,
            max = 2,
            permission = "citizens.npc.profession")
    @Requirements(selected = true, ownership = true, types = { EntityType.VILLAGER })
    public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String profession = args.getString(1);
        Profession parsed = Util.matchEnum(Profession.values(), profession.toUpperCase());
        if (parsed == null) {
            throw new CommandException(Messages.INVALID_PROFESSION);
        }
        npc.getTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.sendTr(sender, Messages.PROFESSION_SET, npc.getName(), profession);
    }

    @Command(aliases = { "npc" }, usage = "remove|rem (all|id|name)", desc = "Remove a NPC", modifiers = { "remove",
    "rem" }, min = 1, max = 2)
    @Requirements
    public void remove(final CommandContext args, final CommandSender sender, NPC npc) throws CommandException {
        if (args.argsLength() == 2) {
            if (args.getString(1).equalsIgnoreCase("all")) {
                if (!sender.hasPermission("citizens.admin.remove.all") && !sender.hasPermission("citizens.admin"))
                    throw new NoPermissionsException();
                npcRegistry.deregisterAll();
                Messaging.sendTr(sender, Messages.REMOVED_ALL_NPCS);
                return;
            } else {
                NPCCommandSelector.Callback callback = new NPCCommandSelector.Callback() {
                    @Override
                    public void run(NPC npc) throws CommandException {
                        if (npc == null)
                            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
                        if (!(sender instanceof ConsoleCommandSender) && !npc.getTrait(Owner.class).isOwnedBy(sender))
                            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
                        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
                            throw new NoPermissionsException();
                        npc.destroy();
                        Messaging.sendTr(sender, Messages.NPC_REMOVED, npc.getName());
                    }
                };
                NPCCommandSelector.startWithCallback(callback, npcRegistry, sender, args, args.getString(1));
                return;
            }
        }
        if (npc == null)
            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
        if (!(sender instanceof ConsoleCommandSender) && !npc.getTrait(Owner.class).isOwnedBy(sender))
            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
        if (!sender.hasPermission("citizens.npc.remove") && !sender.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.destroy();
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
        int nameLength = npc.getTrait(MobType.class).getType() == EntityType.PLAYER ? 16 : 64;
        if (newName.length() > nameLength) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG);
            newName = newName.substring(0, nameLength);
        }
        Location prev = npc.isSpawned() ? npc.getEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(newName);
        if (prev != null) {
            npc.spawn(prev);
        }

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
            usage = "select|sel [id|name] (--r range)",
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
                if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn())
                    throw new CommandException(Messages.NPC_NOT_FOUND);
                if (npc != null && toSelect.getId() == npc.getId())
                    throw new CommandException(Messages.NPC_ALREADY_SELECTED);
                selector.select(sender, toSelect);
                Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
            }
        };
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
                NPC test = npcRegistry.getNPC(possibleNPC);
                if (test == null)
                    continue;
                callback.run(test);
                break;
            }
        } else {
            NPCCommandSelector.startWithCallback(callback, npcRegistry, sender, args, args.getString(1));
        }
    }

    @Command(aliases = { "npc" }, usage = "skeletontype [type]", desc = "Sets the NPC's skeleton type", modifiers = {
            "skeletontype", "sktype" }, min = 2, max = 2, permission = "citizens.npc.skeletontype")
    @Requirements(selected = true, ownership = true, types = EntityType.SKELETON)
    public void skeletonType(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SkeletonType type = SkeletonType.valueOf(args.getString(1).toUpperCase());
        if (type == null)
            throw new CommandException(Messages.INVALID_SKELETON_TYPE);
        npc.getTrait(NPCSkeletonType.class).setType(type);
        Messaging.sendTr(sender, Messages.SKELETON_TYPE_SET, npc.getName(), type);
    }

    @Command(
            aliases = { "npc" },
            usage = "skin (-c) [name]",
            desc = "Sets an NPC's skin name",
            modifiers = { "skin" },
            min = 1,
            max = 2,
            permission = "citizens.npc.skin")
    @Requirements(types = EntityType.PLAYER)
    public void skin(final CommandContext args, final CommandSender sender, final NPC npc) throws CommandException {
        String skinName = npc.getName();
        if (args.hasFlag('c')) {
            npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
        } else {
            if (args.argsLength() != 2)
                throw new CommandException();
            npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, args.getString(1));
            skinName = args.getString(1);
        }
        Messaging.sendTr(sender, Messages.SKIN_SET, npc.getName(), skinName);
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation());
        }
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
        SlimeSize trait = npc.getTrait(SlimeSize.class);
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
            usage = "sound (--death [death sound|d]) (--ambient [ambient sound|d]) (--hurt [hurt sound|d]) (-n(one)) (-d(efault))",
            desc = "Sets an NPC's played sounds",
            modifiers = { "sound" },
            flags = "dn",
            min = 1,
            max = 1,
            permission = "citizens.npc.sound")
    @Requirements(selected = true, ownership = true, livingEntity = true, excludedTypes = { EntityType.PLAYER })
    public void sound(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String ambientSound = npc.data().get(NPC.AMBIENT_SOUND_METADATA);
        String deathSound = npc.data().get(NPC.DEATH_SOUND_METADATA);
        String hurtSound = npc.data().get(NPC.HURT_SOUND_METADATA);
        if (args.getValueFlags().size() == 0 && args.getFlags().size() == 0) {
            Messaging.sendTr(sender, Messages.SOUND_INFO, npc.getName(), ambientSound, hurtSound, deathSound,
                    Util.listValuesPretty(Sound.values()));
            return;
        }

        if (args.hasFlag('n')) {
            ambientSound = deathSound = hurtSound = "";
        }
        if (args.hasFlag('d')) {
            ambientSound = deathSound = hurtSound = null;
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
            npc.data().remove(ambientSound);
        } else {
            npc.data().setPersistent(NPC.AMBIENT_SOUND_METADATA, ambientSound);
        }

        Messaging.sendTr(sender, Messages.SOUND_SET, npc.getName(), ambientSound, hurtSound, deathSound);
    }

    @Command(
            aliases = { "npc" },
            usage = "spawn (id|name)",
            desc = "Spawn an existing NPC",
            modifiers = { "spawn" },
            min = 1,
            max = 2,
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
                Location location = respawn.getTrait(CurrentLocation.class).getLocation();
                if (location == null || args.hasValueFlag("location")) {
                    if (args.getSenderLocation() == null)
                        throw new CommandException(Messages.NO_STORED_SPAWN_LOCATION);

                    location = args.getSenderLocation();
                }
                if (respawn.spawn(location)) {
                    selector.select(sender, respawn);
                    Messaging.sendTr(sender, Messages.NPC_SPAWNED, respawn.getName());
                }
            }
        };
        if (args.argsLength() > 1) {
            NPCCommandSelector.startWithCallback(callback, npcRegistry, sender, args, args.getString(1));
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
        String type = npc.getTrait(Speech.class).getDefaultVocalChord();
        String message = Colorizer.parseColors(args.getJoinedStrings(1));

        if (message.length() <= 0) {
            Messaging.send(sender, "Default Vocal Chord for " + npc.getName() + ": "
                    + npc.getTrait(Speech.class).getDefaultVocalChord());
            return;
        }

        SpeechContext context = new SpeechContext(message);

        if (args.hasValueFlag("target")) {
            if (args.getFlag("target").matches("\\d+")) {
                NPC target = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(args.getFlag("target")));
                if (target != null)
                    context.addRecipient(target.getEntity());
            } else {
                Player player = Bukkit.getPlayer(args.getFlag("target"));
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
        boolean swim = args.hasValueFlag("set") ? Boolean.parseBoolean(args.getFlag("set")) : !npc.data().get(
                NPC.SWIMMING_METADATA, true);
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
        Location to = npc.getTrait(CurrentLocation.class).getLocation();
        if (to == null) {
            Messaging.sendError(player, Messages.TELEPORT_NPC_LOCATION_NOT_FOUND);
            return;
        }
        player.teleport(to, TeleportCause.COMMAND);
        Messaging.sendTr(player, Messages.TELEPORTED_TO_NPC, npc.getName());
    }

    @Command(aliases = { "npc" }, usage = "tphere", desc = "Teleport a NPC to your location", modifiers = { "tphere",
            "tph", "move" }, min = 1, max = 1, permission = "citizens.npc.tphere")
    public void tphere(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.getSenderLocation() == null)
            throw new ServerCommandException();
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned()) {
            npc.spawn(args.getSenderLocation());
            if (!sender.hasPermission("citizens.npc.tphere.multiworld")
                    && npc.getEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
                npc.despawn(DespawnReason.REMOVAL);
                throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
            }
        } else {
            if (!sender.hasPermission("citizens.npc.tphere.multiworld")
                    && npc.getEntity().getLocation().getWorld() != args.getSenderLocation().getWorld()) {
                npc.despawn(DespawnReason.REMOVAL);
                throw new CommandException(Messages.CANNOT_TELEPORT_ACROSS_WORLDS);
            }
            npc.teleport(args.getSenderLocation(), TeleportCause.COMMAND);
        }
        Messaging.sendTr(sender, Messages.NPC_TELEPORTED, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "tpto [player name|npc id] [player name|npc id]",
            desc = "Teleport an NPC or player to another NPC or player",
            modifiers = { "tpto" },
            min = 3,
            max = 3,
            permission = "citizens.npc.tpto")
    @Requirements
    public void tpto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Entity from = null, to = null;
        if (npc != null) {
            from = npc.getEntity();
        }
        boolean firstWasPlayer = false;
        try {
            int id = args.getInteger(1);
            NPC fromNPC = CitizensAPI.getNPCRegistry().getById(id);
            if (fromNPC != null) {
                from = fromNPC.getEntity();
            }
        } catch (NumberFormatException e) {
            from = Bukkit.getPlayerExact(args.getString(1));
            firstWasPlayer = true;
        }
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
            usage = "wolf (-s(itting) a(ngry) t(amed)) --collar [hex rgb color|name]",
            desc = "Sets wolf modifiers",
            modifiers = { "wolf" },
            min = 1,
            max = 1,
            flags = "sat",
            permission = "citizens.npc.wolf")
    @Requirements(selected = true, ownership = true, types = EntityType.WOLF)
    public void wolf(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        WolfModifiers trait = npc.getTrait(WolfModifiers.class);
        trait.setAngry(args.hasFlag('a'));
        trait.setSitting(args.hasFlag('s'));
        trait.setTamed(args.hasFlag('t'));
        if (args.hasValueFlag("collar")) {
            String unparsed = args.getFlag("collar");
            DyeColor color = null;
            try {
                color = DyeColor.valueOf(unparsed.toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException e) {
                int rgb = Integer.parseInt(unparsed.replace("#", ""), 16);
                color = DyeColor.getByColor(org.bukkit.Color.fromRGB(rgb));
            }
            if (color == null)
                throw new CommandException(Messages.COLLAR_COLOUR_NOT_RECOGNISED);
            trait.setCollarColor(color);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "zombiemod (-b(aby), -v(illager))",
            desc = "Sets a zombie NPC to be a baby or villager",
            modifiers = { "zombie", "zombiemod" },
            flags = "bv",
            min = 1,
            max = 1,
            permission = "citizens.npc.zombiemodifier")
    @Requirements(selected = true, ownership = true, types = { EntityType.ZOMBIE, EntityType.PIG_ZOMBIE })
    public void zombieModifier(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ZombieModifier trait = npc.getTrait(ZombieModifier.class);
        if (args.hasFlag('b')) {
            boolean isBaby = trait.toggleBaby();
            Messaging.sendTr(sender, isBaby ? Messages.ZOMBIE_BABY_SET : Messages.ZOMBIE_BABY_UNSET, npc.getName());
        }
        if (args.hasFlag('v')) {
            boolean isVillager = trait.toggleVillager();
            Messaging.sendTr(sender, isVillager ? Messages.ZOMBIE_VILLAGER_SET : Messages.ZOMBIE_VILLAGER_UNSET,
                    npc.getName());
        }
    }
}
