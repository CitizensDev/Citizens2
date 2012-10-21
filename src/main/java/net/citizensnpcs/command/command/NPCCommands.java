package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Behaviour;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.Pose;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
            permission = "npc.age")
    @Requirements(selected = true, ownership = true, types = { EntityType.CHICKEN, EntityType.COW,
            EntityType.OCELOT, EntityType.PIG, EntityType.SHEEP, EntityType.VILLAGER, EntityType.WOLF })
    public void age(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
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
            if (age < -24000 || age > 0)
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
            usage = "anchor (--save [name]|--assume [name]|--remove [name]) (-a)",
            desc = "Changes/Saves/Lists NPC's location anchor(s)",
            flags = "a",
            modifiers = { "anchor" },
            min = 1,
            max = 2,
            permission = "npc.anchor")
    @Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
    public void anchor(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Anchors trait = npc.getTrait(Anchors.class);
        if (args.hasValueFlag("save")) {
            if (args.getFlag("save").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            if (!(sender instanceof Player))
                throw new ServerCommandException();

            if (trait.addAnchor(args.getFlag("save"), ((Player) sender).getLocation())) {
                Messaging.sendTr(sender, Messages.ANCHOR_ADDED);
            } else
                throw new CommandException(Messages.ANCHOR_ALREADY_EXISTS, args.getFlag("save"));
        } else if (args.hasValueFlag("assume")) {
            if (args.getFlag("assume").isEmpty())
                throw new CommandException(Messages.INVALID_ANCHOR_NAME);

            Anchor anchor = trait.getAnchor(args.getFlag("assume"));
            if (anchor == null)
                throw new CommandException(Messages.ANCHOR_MISSING, args.getFlag("assume"));
            npc.getBukkitEntity().teleport(anchor.getLocation());
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
                String line = "<a>" + i + "<b>  " + trait.getAnchors().get(i).getName() + "<c>  "
                        + trait.getAnchors().get(i).getLocation().getWorld().getName() + "<d>  "
                        + trait.getAnchors().get(i).getLocation().getBlockX() + ", "
                        + trait.getAnchors().get(i).getLocation().getBlockY() + ", "
                        + trait.getAnchors().get(i).getLocation().getBlockZ();
                paginator.addLine(line);
            }

            int page = args.getInteger(1, 1);
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
        }

        // Assume Player's position
        if (!args.hasFlag('a'))
            return;
        if (sender instanceof Player) {
            Location location = ((Player) sender).getLocation();
            npc.getBukkitEntity().teleport(location);
        } else
            throw new ServerCommandException();
    }

    @Command(
            aliases = { "npc" },
            usage = "behaviour [scripts] (-r)",
            desc = "Sets the behaviour of a NPC",
            help = Messages.BEHAVIOUR_HELP,
            modifiers = { "behaviour", "ai" },
            flags = "r",
            min = 2,
            permission = "npc.behaviour")
    public void behaviour(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Iterable<String> files = Splitter.on(',').split(args.getJoinedStrings(1, ','));
        if (args.hasFlag('r')) {
            npc.getTrait(Behaviour.class).removeScripts(files);
            Messaging.sendTr(sender, Messages.BEHAVIOURS_REMOVED);
        } else {
            npc.getTrait(Behaviour.class).addScripts(files);
            Messaging.sendTr(sender, Messages.BEHAVIOURS_ADDED);
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "controllable|control -f",
            desc = "Toggles whether the NPC can be ridden and controlled",
            modifiers = { "controllable", "control" },
            min = 1,
            max = 1,
            flags = "f",
            permission = "npc.controllable")
    public void controllable(CommandContext args, CommandSender sender, NPC npc) {
        Controllable trait = npc.getTrait(Controllable.class);
        boolean enabled = trait.toggle();
        String key = enabled ? Messages.CONTROLLABLE_SET : Messages.CONTROLLABLE_REMOVED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "copy (--name newname)",
            desc = "Copies an NPC",
            modifiers = { "copy" },
            min = 1,
            max = 1,
            permission = "npc.copy")
    public void copy(CommandContext args, CommandSender sender, NPC npc) {
        EntityType type = npc.getTrait(MobType.class).getType();
        String name = args.getFlag("name", npc.getFullName());
        CitizensNPC copy = (CitizensNPC) npcRegistry.createNPC(type, name);
        CitizensNPC from = (CitizensNPC) npc;

        DataKey key = new MemoryDataKey();
        from.save(key);
        copy.load(key);

        if (copy.isSpawned() && sender instanceof Player) {
            Player player = (Player) sender;
            copy.getBukkitEntity().teleport(player);
            copy.getTrait(CurrentLocation.class).setLocation(player.getLocation());
        }

        for (Trait trait : copy.getTraits())
            trait.onCopy();
        Messaging.sendTr(sender, Messages.NPC_COPIED, npc.getName());
        selector.select(sender, copy);
    }

    @Command(
            aliases = { "npc" },
            usage = "create [name] ((-b -u) --type (type) --trait ('trait1, trait2...') --b (behaviour))",
            desc = "Create a new NPC",
            flags = "bu",
            modifiers = { "create" },
            min = 2,
            permission = "npc.create")
    @Requirements
    public void create(CommandContext args, final Player player, NPC npc) throws CommandException {
        String name = StringHelper.parseColors(args.getJoinedStrings(1));
        if (name.length() > 16) {
            Messaging.sendErrorTr(player, Messages.NPC_NAME_TOO_LONG);
            name = name.substring(0, 15);
        }
        EntityType type = EntityType.PLAYER;
        if (args.hasValueFlag("type")) {
            String inputType = args.getFlag("type");
            type = Util.matchEntityType(inputType);
            if (type == null) {
                Messaging.sendErrorTr(player, Messages.NPC_CREATE_INVALID_MOBTYPE, inputType);
                type = EntityType.PLAYER;
            } else if (!LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                Messaging.sendErrorTr(player, Messages.NOT_LIVING_MOBTYPE, type);
                type = EntityType.PLAYER;
            }
        }

        npc = npcRegistry.createNPC(type, name);
        String msg = "You created [[" + npc.getName() + "]] at your location";

        int age = 0;
        if (args.hasFlag('b')) {
            if (!Ageable.class.isAssignableFrom(type.getEntityClass()))
                Messaging.sendErrorTr(player, Messages.MOBTYPE_CANNOT_BE_AGED, type.name().toLowerCase()
                        .replace("_", "-"));
            else {
                age = -24000;
                msg += " as a baby";
            }
        }

        if (args.hasValueFlag("b")) {
            npc.getTrait(Behaviour.class).addScripts(Splitter.on(",").split(args.getFlag("behaviour")));
            msg += " with the specified behaviours";
        }

        msg += ".";

        // Initialize necessary traits
        if (!Setting.SERVER_OWNS_NPCS.asBoolean())
            npc.getTrait(Owner.class).setOwner(player.getName());
        npc.getTrait(MobType.class).setType(type);

        if (!args.hasFlag('u'))
            npc.spawn(player.getLocation());

        PlayerCreateNPCEvent event = new PlayerCreateNPCEvent(player, npc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            npc.destroy();
            String reason = "Couldn't create NPC.";
            if (!event.getCancelReason().isEmpty())
                reason += " Reason: " + event.getCancelReason();
            throw new CommandException(reason);
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
        if (npc.getBukkitEntity() instanceof Ageable)
            npc.getTrait(Age.class).setAge(age);
        selector.select(player, npc);
        Messaging.send(player, msg);
    }

    @Command(
            aliases = { "npc" },
            usage = "despawn (id)",
            desc = "Despawn a NPC",
            modifiers = { "despawn" },
            min = 1,
            max = 2,
            permission = "npc.despawn")
    @Requirements
    public void despawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc == null || args.argsLength() == 2) {
            if (args.argsLength() < 2)
                throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
            int id = args.getInteger(1);
            npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc == null)
                throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, id);
        }
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn();
        Messaging.sendTr(sender, Messages.NPC_DESPAWNED, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "gravity",
            desc = "Toggles gravity",
            modifiers = { "gravity" },
            min = 1,
            max = 1,
            permission = "npc.gravity")
    public void gravity(CommandContext args, CommandSender sender, NPC npc) {
        boolean enabled = npc.getTrait(Gravity.class).toggle();
        String key = enabled ? Messages.GRAVITY_ENABLED : Messages.GRAVITY_DISABLED;
        Messaging.sendTr(sender, key, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "list (page) ((-a) --owner (owner) --type (type) --char (char))",
            desc = "List NPCs",
            flags = "a",
            modifiers = { "list" },
            min = 1,
            max = 2,
            permission = "npc.list")
    @Requirements
    public void list(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : npcRegistry)
                npcs.add(add);
        } else if (args.getValueFlags().size() == 0 && sender instanceof Player) {
            for (NPC add : npcRegistry) {
                if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(sender))
                    npcs.add(add);
            }
        } else {
            if (args.hasValueFlag("owner")) {
                String name = args.getFlag("owner");
                for (NPC add : npcRegistry) {
                    if (!npcs.contains(add) && add.getTrait(Owner.class).isOwnedBy(name))
                        npcs.add(add);
                }
            }

            if (args.hasValueFlag("type")) {
                EntityType type = Util.matchEntityType(args.getFlag("type"));

                if (type == null)
                    throw new CommandException(Messages.COMMAND_INVALID_MOBTYPE, type);

                for (NPC add : npcRegistry) {
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
            permission = "npc.lookclose")
    public void lookClose(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, npc.getTrait(LookClose.class).toggle() ? Messages.LOOKCLOSE_SET
                : Messages.LOOKCLOSE_STOPPED, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "mount",
            desc = "Mounts a controllable NPC",
            modifiers = { "mount" },
            min = 1,
            max = 1,
            permission = "npc.controllable")
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
            permission = "npc.moveto")
    public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        Location current = npc.getBukkitEntity().getLocation();
        Location to;
        if (args.argsLength() > 1) {
            String[] parts = Iterables.toArray(Splitter.on(':').split(args.getJoinedStrings(1, ':')),
                    String.class);
            if (parts.length != 4 && parts.length != 3)
                throw new CommandException(Messages.MOVETO_FORMAT);
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = parts.length == 4 ? Bukkit.getWorld(parts[3]) : current.getWorld();
            if (world == null)
                throw new CommandException(Messages.MOVETO_WORLD_NOT_FOUND);
            to = new Location(world, x, y, z, current.getYaw(), current.getPitch());
        } else {
            to = current.clone();
            if (args.hasValueFlag("x"))
                to.setX(args.getFlagInteger("x"));
            if (args.hasValueFlag("y"))
                to.setY(args.getFlagInteger("y"));
            if (args.hasValueFlag("z"))
                to.setZ(args.getFlagInteger("z"));
            if (args.hasValueFlag("yaw"))
                to.setYaw((float) args.getFlagDouble("yaw"));
            if (args.hasValueFlag("pitch"))
                to.setPitch((float) args.getFlagDouble("pitch"));
            if (args.hasValueFlag("world")) {
                World world = Bukkit.getWorld(args.getFlag("world"));
                if (world == null)
                    throw new CommandException(Messages.MOVETO_WORLD_NOT_FOUND);
                to.setWorld(world);
            }
        }

        npc.getBukkitEntity().teleport(to, TeleportCause.COMMAND);

        Messaging.sendTr(sender, Messages.MOVETO_TELEPORTED, npc.getName(), to);
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0)
    public void npc(CommandContext args, CommandSender sender, final NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
        Messaging.send(sender, "    <a>Type: <e>" + npc.getTrait(MobType.class).getType());
        Messaging.send(sender, "    <a>Traits<e>");
        for (Trait trait : npc.getTraits()) {
            if (CitizensAPI.getTraitFactory().isInternalTrait(trait))
                continue;
            Messaging.send(sender, "     <e>- <a>" + trait.getName() + "<e>");
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "owner [name]",
            desc = "Set the owner of an NPC",
            modifiers = { "owner" },
            min = 1,
            max = 2,
            permission = "npc.owner")
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
        Messaging.sendTr(sender, serverOwner ? Messages.OWNER_SET_SERVER : Messages.OWNER_SET, npc.getName(),
                name);
    }

    @Command(
            aliases = { "npc" },
            usage = "pathrange [range]",
            desc = "Sets an NPC's pathfinding range",
            modifiers = { "pathrange", "pathfindingrange", "prange" },
            min = 2,
            max = 2,
            permission = "npc.pathfindingrange")
    public void pathfindingRange(CommandContext args, CommandSender sender, NPC npc) {
        double range = Math.max(1, args.getDouble(1));
        npc.getNavigator().getDefaultParameters().range((float) range);
        Messaging.sendTr(sender, Messages.PATHFINDING_RANGE_SET, range);
    }

    @Command(
            aliases = { "npc" },
            usage = "pose (--save [name]|--assume [name]|--remove [name]) (-a)",
            desc = "Changes/Saves/Lists NPC's head pose(s)",
            flags = "a",
            modifiers = { "pose" },
            min = 1,
            max = 2,
            permission = "npc.pose")
    @Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
    public void pose(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Poses trait = npc.getTrait(Poses.class);
        if (args.hasValueFlag("save")) {
            if (args.getFlag("save").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            if (!(sender instanceof Player))
                throw new ServerCommandException();

            if (trait.addPose(args.getFlag("save"), ((Player) sender).getLocation())) {
                Messaging.sendTr(sender, Messages.POSE_ADDED);
            } else
                throw new CommandException(Messages.POSE_ALREADY_EXISTS, args.getFlag("assume"));
        } else if (args.hasValueFlag("assume")) {
            if (args.getFlag("assume").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);

            Pose pose = trait.getPose(args.getFlag("assume"));
            if (pose == null)
                throw new CommandException(Messages.POSE_MISSING, args.getFlag("assume"));
            trait.assumePose(pose);
        } else if (args.hasValueFlag("remove")) {
            if (args.getFlag("remove").isEmpty())
                throw new CommandException(Messages.INVALID_POSE_NAME);
            if (trait.removePose(trait.getPose(args.getFlag("remove"))))
                Messaging.sendTr(sender, Messages.POSE_REMOVED);
            else
                throw new CommandException(Messages.POSE_MISSING, args.getFlag("remove"));
        } else if (!args.hasFlag('a')) {
            Paginator paginator = new Paginator().header("Pose");
            paginator.addLine("<e>Key: <a>ID  <b>Name  <c>Pitch/Yaw");
            for (int i = 0; i < trait.getPoses().size(); i++) {
                String line = "<a>" + i + "<b>  " + trait.getPoses().get(i).getName() + "<c>  "
                        + trait.getPoses().get(i).getPitch() + "/" + trait.getPoses().get(i).getYaw();
                paginator.addLine(line);
            }

            int page = args.getInteger(1, 1);
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
        }

        // Assume Player's pose
        if (!args.hasFlag('a'))
            return;
        if (sender instanceof Player) {
            Location location = ((Player) sender).getLocation();
            trait.assumePose(new Pose(sender.getName(), location.getPitch(), location.getYaw()));
        } else
            throw new ServerCommandException();
    }

    @Command(
            aliases = { "npc" },
            usage = "power",
            desc = "Toggle a creeper NPC as powered",
            modifiers = { "power" },
            min = 1,
            max = 1,
            permission = "npc.power")
    @Requirements(selected = true, ownership = true, types = { EntityType.CREEPER })
    public void power(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, npc.getTrait(Powered.class).toggle() ? Messages.POWERED_SET
                : Messages.POWERED_STOPPED);
    }

    @Command(
            aliases = { "npc" },
            usage = "profession|prof [profession]",
            desc = "Set a NPC's profession",
            modifiers = { "profession", "prof" },
            min = 2,
            max = 2,
            permission = "npc.profession")
    @Requirements(selected = true, ownership = true, types = { EntityType.VILLAGER })
    public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String profession = args.getString(1);
        Profession parsed;
        try {
            parsed = Profession.valueOf(profession.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CommandException(Messages.INVALID_PROFESSION);
        }
        npc.getTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.sendTr(sender, Messages.PROFESSION_SET, npc.getName(), profession);
    }

    @Command(aliases = { "npc" }, usage = "remove|rem (all)", desc = "Remove a NPC", modifiers = { "remove",
            "rem" }, min = 1, max = 2)
    @Requirements
    public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.argsLength() == 2) {
            if (!args.getString(1).equalsIgnoreCase("all"))
                throw new CommandException(Messages.REMOVE_INCORRECT_SYNTAX);
            if (!sender.hasPermission("citizens.npc.remove.all") && !sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            npcRegistry.deregisterAll();
            Messaging.sendTr(sender, Messages.REMOVED_ALL_NPCS);
            return;
        }
        if (!(sender instanceof Player))
            throw new CommandException(Messages.COMMAND_MUST_BE_INGAME);
        Player player = (Player) sender;
        if (npc == null)
            throw new CommandException(Messages.COMMAND_MUST_HAVE_SELECTED);
        if (!npc.getTrait(Owner.class).isOwnedBy(player))
            throw new CommandException(Messages.COMMAND_MUST_BE_OWNER);
        if (!player.hasPermission("citizens.npc.remove") && !player.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.destroy();
        Messaging.sendTr(player, Messages.NPC_REMOVED, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "rename [name]",
            desc = "Rename a NPC",
            modifiers = { "rename" },
            min = 2,
            permission = "npc.rename")
    public void rename(CommandContext args, CommandSender sender, NPC npc) {
        String oldName = npc.getName();
        String newName = args.getJoinedStrings(1);
        if (newName.length() > 16) {
            Messaging.sendErrorTr(sender, Messages.NPC_NAME_TOO_LONG);
            newName = newName.substring(0, 15);
        }
        npc.setName(newName);
        Messaging.sendTr(sender, Messages.NPC_RENAMED, oldName, newName);
    }

    @Command(
            aliases = { "npc" },
            usage = "select|sel [id|name] (--r range)",
            desc = "Select a NPC with the given ID or name",
            modifiers = { "select", "sel" },
            min = 1,
            max = 2,
            permission = "npc.select")
    @Requirements
    public void select(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPC toSelect = null;
        if (args.argsLength() <= 1) {
            if (!(sender instanceof Player))
                throw new ServerCommandException();
            double range = Math.abs(args.getFlagDouble("r", 10));
            Player player = (Player) sender;
            final Location location = player.getLocation();
            List<Entity> search = player.getNearbyEntities(range, range, range);
            Collections.sort(search, new Comparator<Entity>() {
                @Override
                public int compare(Entity o1, Entity o2) {
                    double d = o1.getLocation().distanceSquared(location)
                            - o2.getLocation().distanceSquared(location);
                    return d > 0 ? 1 : d < 0 ? -1 : 0;
                }
            });
            for (Entity possibleNPC : search) {
                NPC test = npcRegistry.getNPC(possibleNPC);
                if (test == null)
                    continue;
                toSelect = test;
                break;
            }
        } else {
            try {
                int id = args.getInteger(1);
                toSelect = npcRegistry.getById(id);
            } catch (NumberFormatException ex) {
                String name = args.getString(1);
                List<NPC> possible = Lists.newArrayList();
                for (NPC test : npcRegistry) {
                    if (test.getName().equalsIgnoreCase(name))
                        possible.add(test);
                }
                if (possible.size() == 1)
                    toSelect = possible.get(0);
                else if (possible.size() > 1) {
                    SelectionPrompt.start(selector, (Player) sender, possible);
                    return;
                }
            }
        }
        if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn())
            throw new CommandException(Messages.NPC_NOT_FOUND);
        if (npc != null && toSelect.getId() == npc.getId())
            throw new CommandException(Messages.NPC_ALREADY_SELECTED);
        selector.select(sender, toSelect);
        Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
    }

    @Command(
            aliases = { "npc" },
            usage = "size [size]",
            desc = "Sets the NPC's size",
            modifiers = { "size" },
            min = 1,
            max = 2,
            permission = "npc.size")
    @Requirements(selected = true, ownership = true, types = { EntityType.MAGMA_CUBE, EntityType.SLIME })
    public void slimeSize(CommandContext args, CommandSender sender, NPC npc) {
        SlimeSize trait = npc.getTrait(SlimeSize.class);
        if (args.argsLength() <= 1) {
            trait.describe(sender);
            return;
        }
        int size = Math.max(1, args.getInteger(1));
        trait.setSize(size);
        Messaging.sendTr(sender, Messages.SIZE_SET, npc.getName(), size);
    }

    @Command(
            aliases = { "npc" },
            usage = "spawn [id]",
            desc = "Spawn an existing NPC",
            modifiers = { "spawn" },
            min = 2,
            max = 2,
            permission = "npc.spawn")
    @Requirements(ownership = true)
    public void spawn(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPC respawn = npcRegistry.getById(args.getInteger(1));
        if (respawn == null)
            throw new CommandException(Messages.NO_NPC_WITH_ID_FOUND, args.getInteger(1));
        if (respawn.isSpawned())
            throw new CommandException(Messages.NPC_ALREADY_SPAWNED, respawn.getName());

        Location location = respawn.getTrait(CurrentLocation.class).getLocation();
        if (location == null) {
            if (!(sender instanceof Player))
                throw new CommandException(Messages.NO_STORED_SPAWN_LOCATION);

            location = ((Player) sender).getLocation();
        }
        if (respawn.spawn(location)) {
            selector.select(sender, respawn);
            Messaging.sendTr(sender, Messages.NPC_SPAWNED, respawn.getName());
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "speed [speed]",
            desc = "Sets the movement speed of an NPC as a percentage",
            modifiers = { "speed" },
            min = 2,
            max = 2,
            permission = "npc.speed")
    public void speed(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        float newSpeed = (float) Math.abs(args.getDouble(1));
        if (newSpeed >= Setting.MAX_SPEED.asDouble())
            throw new CommandException(Messages.SPEED_MODIFIER_ABOVE_LIMIT);
        npc.getNavigator().getDefaultParameters().speedModifier(newSpeed);

        Messaging.sendTr(sender, Messages.SPEED_MODIFIER_SET, newSpeed);
    }

    @Command(
            aliases = { "npc" },
            usage = "tp",
            desc = "Teleport to a NPC",
            modifiers = { "tp", "teleport" },
            min = 1,
            max = 1,
            permission = "npc.tp")
    public void tp(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        player.teleport(npc.getBukkitEntity(), TeleportCause.COMMAND);
        Messaging.sendTr(player, Messages.TELEPORTED_TO_NPC, npc.getName());
    }

    @Command(aliases = { "npc" }, usage = "tphere", desc = "Teleport a NPC to your location", modifiers = {
            "tphere", "tph", "move" }, min = 1, max = 1, permission = "npc.tphere")
    public void tphere(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        Messaging.sendTr(player, Messages.NPC_TELEPORTED, npc.getName());
    }

    @Command(
            aliases = { "npc" },
            usage = "vulnerable (-t)",
            desc = "Toggles an NPC's vulnerability",
            modifiers = { "vulnerable" },
            min = 1,
            max = 1,
            flags = "t",
            permission = "npc.vulnerable")
    public void vulnerable(CommandContext args, CommandSender sender, NPC npc) {
        boolean vulnerable = !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
        if (args.hasFlag('t')) {
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        } else {
            vulnerable = !npc.data().getPersistent(NPC.DEFAULT_PROTECTED_METADATA, true);
            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, vulnerable);
        }
        String key = vulnerable ? Messages.VULNERABLE_SET : Messages.VULNERABLE_STOPPED;
        Messaging.sendTr(sender, key, npc.getName());
    }
}