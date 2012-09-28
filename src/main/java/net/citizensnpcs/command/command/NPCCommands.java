package net.citizensnpcs.command.command;

import java.util.ArrayList;
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
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Behaviour;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;

import net.minecraft.server.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        if (toggleLock)
            Messaging.send(sender, "<a>Age " + (trait.toggle() ? "locked" : "unlocked") + ".");

        if (args.argsLength() > 1) {
            int age = 0;
            String ageStr = "an adult";
            try {
                age = args.getInteger(1);
                if (age < -24000 || age > 0)
                    throw new CommandException("Invalid age. Valid: adult, baby, number between -24000 and 0");
                ageStr = "age " + StringHelper.wrap(age);
            } catch (NumberFormatException ex) {
                if (args.getString(1).equalsIgnoreCase("baby")) {
                    age = -24000;
                    ageStr = "a baby";
                } else if (!args.getString(1).equalsIgnoreCase("adult"))
                    throw new CommandException("Invalid age. Valid: adult, baby, number between -24000 and 0");
            }

            trait.setAge(age);
            Messaging.sendF(sender, StringHelper.wrap(npc.getName()) + " is now %s.", ageStr);
        } else if (!toggleLock)
            trait.describe(sender);
    }

    @Command(
            aliases = { "npc" },
            usage = "behaviour [scripts] (-r)",
            desc = "Sets the behaviour of a NPC",
            modifiers = { "behaviour", "ai" },
            flags = "r",
            min = 2,
            permission = "npc.behaviour")
    public void behaviour(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Iterable<String> files = Splitter.on(',').split(args.getJoinedStrings(1, ','));
        if (args.hasFlag('r')) {
            npc.getTrait(Behaviour.class).removeScripts(files);
            sender.sendMessage(ChatColor.GREEN + "Behaviours removed.");
        } else {
            npc.getTrait(Behaviour.class).addScripts(files);
            sender.sendMessage(ChatColor.GREEN + "Behaviours added.");
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "controllable|control",
            desc = "Toggles whether the NPC can be ridden and controlled",
            modifiers = { "controllable", "control" },
            min = 1,
            max = 1,
            permission = "npc.controllable")
    public void controllable(CommandContext args, CommandSender sender, NPC npc) {
        boolean enabled = npc.getTrait(Controllable.class).toggle();
        if (enabled) {
            Messaging.send(sender, StringHelper.wrap(npc.getName()) + " can now be controlled.");
        } else {
            Messaging.send(sender, StringHelper.wrap(npc.getName()) + " can no longer be controlled.");
        }
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

        Messaging.sendF(sender, ChatColor.GREEN + "%s has been copied.", StringHelper.wrap(npc.getName()));
        selector.select(sender, npc);
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
            Messaging.sendError(player,
                    "NPC names cannot be longer than 16 characters. The name has been shortened.");
            name = name.substring(0, 15);
        }
        EntityType type = EntityType.PLAYER;
        if (args.hasValueFlag("type")) {
            String inputType = args.getFlag("type");
            type = Util.matchEntityType(inputType);
            if (type == null) {
                Messaging.sendError(player, "'" + inputType
                        + "' is not a valid mob type. Using default type.");
                type = EntityType.PLAYER;
            } else if (!LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                Messaging.sendError(player, "'%s' is not a living entity type. Using default type.");
                type = EntityType.PLAYER;
            }
        }

        npc = npcRegistry.createNPC(type, name);
        String msg = ChatColor.GREEN + "You created " + StringHelper.wrap(npc.getName())
                + " at your location";

        int age = 0;
        if (args.hasFlag('b')) {
            if (!Ageable.class.isAssignableFrom(type.getEntityClass()))
                Messaging.sendError(player, "The mob type '" + type.name().toLowerCase().replace("_", "-")
                        + "' cannot be aged.");
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
            Iterable<String> parts = Splitter.on(",").trimResults().split(args.getFlag("trait"));
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
                throw new CommandException("No NPC selected.");
            npc = CitizensAPI.getNPCRegistry().getById(args.getInteger(1));
            if (npc == null)
                throw new CommandException("No NPC found with that ID.");
        }
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn();
        Messaging.send(sender, ChatColor.GREEN + "You despawned " + StringHelper.wrap(npc.getName()) + ".");
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
                    throw new CommandException("'" + type + "' is not a valid mob type.");

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
            throw new CommandException("The page '" + page + "' does not exist.");
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
        String msg = StringHelper.wrap(npc.getName()) + " will "
                + (npc.getTrait(LookClose.class).toggle() ? "now rotate" : "no longer rotate");
        Messaging.send(sender, msg + " when a player is nearby.");
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
            Messaging.send(player, StringHelper.wrap(npc.getName()) + " is not controllable.");
            return;
        }
        boolean success = npc.getTrait(Controllable.class).mount(player);
        if (!success)
            Messaging.sendF(player, ChatColor.GREEN + "Couldn't mount %s.", StringHelper.wrap(npc.getName()));
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
                throw new CommandException("Format is x:y:z(:world) or x y z( world)");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            World world = parts.length == 4 ? Bukkit.getWorld(parts[3]) : current.getWorld();
            if (world == null)
                throw new CommandException("world not found");
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
                    throw new CommandException("Given world not found.");
                to.setWorld(world);
            }
        }

        npc.getBukkitEntity().teleport(to, TeleportCause.COMMAND);
        Messaging.send(sender, StringHelper.wrap(npc.getName()) + " was teleported to " + to + ".");
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
        if (args.argsLength() == 1) {
            Messaging.send(sender, StringHelper.wrap(npc.getName() + "'s Owner: ")
                    + npc.getTrait(Owner.class).getOwner());
            return;
        }
        String name = args.getString(1);
        if (npc.getTrait(Owner.class).isOwnedBy(name))
            throw new CommandException("'" + name + "' is already the owner of " + npc.getName() + ".");
        npc.getTrait(Owner.class).setOwner(name);
        Messaging.send(sender, (name.equalsIgnoreCase("server") ? "<a>The server" : StringHelper.wrap(name))
                + " is now the owner of " + StringHelper.wrap(npc.getName()) + ".");
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
        String msg = StringHelper.wrap(npc.getName()) + " will "
                + (npc.getTrait(Powered.class).toggle() ? "now" : "no longer");
        Messaging.send(sender, msg += " be powered.");
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
            throw new CommandException("'" + profession + "' is not a valid profession.");
        }
        npc.getTrait(VillagerProfession.class).setProfession(parsed);
        Messaging.send(sender,
                StringHelper.wrap(npc.getName()) + " is now a " + StringHelper.wrap(profession) + ".");
    }

    @Command(aliases = { "npc" }, usage = "remove|rem (all)", desc = "Remove a NPC", modifiers = { "remove",
            "rem" }, min = 1, max = 2)
    @Requirements
    public void remove(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (args.argsLength() == 2) {
            if (!args.getString(1).equalsIgnoreCase("all"))
                throw new CommandException("Incorrect syntax. /npc remove (all)");
            if (!sender.hasPermission("citizens.npc.remove.all") && !sender.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            npcRegistry.deregisterAll();
            Messaging.send(sender, "<a>You permanently removed all NPCs.");
            return;
        }
        if (!(sender instanceof Player))
            throw new CommandException("You must be ingame to use this command");
        Player player = (Player) sender;
        if (npc == null)
            throw new CommandException("You must have an NPC selected to execute that command.");
        if (!npc.getTrait(Owner.class).isOwnedBy(player))
            throw new CommandException("You must be the owner of this NPC to execute that command.");
        if (!player.hasPermission("citizens.npc.remove") && !player.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.destroy();
        Messaging.send(player, "<a>You permanently removed " + StringHelper.wrap(npc.getName()) + ".");
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
            Messaging.sendError(sender,
                    "NPC names cannot be longer than 16 characters. The name has been shortened.");
            newName = newName.substring(0, 15);
        }
        npc.setName(newName);
        String msg = String.format("You renamed %s to %s.", StringHelper.wrap(oldName),
                StringHelper.wrap(newName));
        Messaging.send(sender, ChatColor.GREEN + msg);
    }

    @Command(
            aliases = { "npc" },
            usage = "select|sel [id]",
            desc = "Select a NPC with the given ID",
            modifiers = { "select", "sel" },
            min = 2,
            max = 2,
            permission = "npc.select")
    @Requirements(ownership = true)
    public void select(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NPC toSelect = npcRegistry.getById(args.getInteger(1));
        if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn())
            throw new CommandException("No NPC with the ID '" + args.getInteger(1) + "' is spawned.");
        if (npc != null && toSelect.getId() == npc.getId())
            throw new CommandException("You already have that NPC selected.");
        selector.select(sender, toSelect);
        Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
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
            throw new CommandException("No NPC with the ID '" + args.getInteger(1) + "' exists.");
        if (respawn.isSpawned())
            throw new CommandException(respawn.getName() + " is already spawned at another location."
                    + " Use '/npc tphere' to teleport the NPC to your location.");

        Location location = respawn.getTrait(CurrentLocation.class).getLocation();
        if (location == null) {
            if (sender instanceof Player)
                location = ((Player) sender).getLocation();
            else
                throw new CommandException("No stored location available - command must be used ingame.");
        }
        if (respawn.spawn(location)) {
            selector.select(sender, respawn);
            Messaging.send(sender, ChatColor.GREEN + "You spawned " + StringHelper.wrap(respawn.getName())
                    + ".");
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
            throw new CommandException("Speed is above the limit.");
        npc.getNavigator().getDefaultParameters().speedModifier(newSpeed);

        Messaging.sendF(sender, ChatColor.GREEN + "NPC speed modifier set to %s.",
                StringHelper.wrap(newSpeed));
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
        Messaging.send(player, ChatColor.GREEN + "You teleported to " + StringHelper.wrap(npc.getName())
                + ".");
    }

    @Command(aliases = { "npc" }, usage = "tphere", desc = "Teleport a NPC to your location", modifiers = {
            "tphere", "tph", "move" }, min = 1, max = 1, permission = "npc.tphere")
    public void tphere(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
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
        boolean vulnerable = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
        if (args.hasFlag('t'))
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, !vulnerable);
        else
            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, !vulnerable);

        Messaging.sendF(sender, ChatColor.GREEN + "%s is %s vulnerable.", StringHelper.wrap(npc.getName()),
                vulnerable ? "now" : "no longer");
    }
    
    @Command(
            aliases = { "npc" },
            usage = "position (-a)",
            desc = "Changes NPC's head position",
            flags = "a",
            modifiers = { "position" },
            min = 1,
            max = 2,
            permission = "npc.position.assume")
    @Requirements(selected = true, ownership = true, types = { EntityType.PLAYER })
    public void position(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        
    	// Assume Player's position
    	if (args.hasFlag('a')) { 
    		if (sender instanceof Player) {
    			// Spawn the NPC if it isn't spawned to prevent NPEs
    			if (!npc.isSpawned())
    	            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
    			
    			// Update entity with some NMS magic
    			EntityLiving handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
    			handle.yaw = (float) ((Player) sender).getLocation().getYaw();
    			handle.pitch = (float) ((Player) sender).getLocation().getPitch();
    			handle.as = handle.yaw;
    			
    			return;
    		}
    		
    		else 
    			Messaging.sendF(sender, ChatColor.YELLOW + "This command can only be used by a Player in-game");
    	}
    	
    	Messaging.sendF(sender, ChatColor.YELLOW + "Usage: '/npc position -a' to assume Player's head position");
    }
}