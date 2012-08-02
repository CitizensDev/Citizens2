package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.NoPermissionsException;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Splitter;

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

        if (args.argsLength() > 1) {
            int age = 0;
            String ageStr = "an adult";
            try {
                age = args.getInteger(1);
                if (age < -24000 || age > 0)
                    throw new CommandException("Invalid age. Valid: adult, baby, number between -24000 and 0");
                ageStr = "age " + age;
            } catch (NumberFormatException ex) {
                if (args.getString(1).equalsIgnoreCase("baby")) {
                    age = -24000;
                    ageStr = "a baby";
                } else if (!args.getString(1).equalsIgnoreCase("adult"))
                    throw new CommandException("Invalid age. Valid: adult, baby, number between -24000 and 0");
            }

            trait.setAge(age);
            Messaging.send(sender, StringHelper.wrap(npc.getName()) + " is now " + ageStr + ".");
        }

        if (args.hasFlag('l'))
            Messaging.send(sender, "<a>Age " + (trait.toggle() ? "locked" : "unlocked") + ".");
    }

    @Command(
            aliases = { "npc" },
            usage = "behaviour [scripts]",
            desc = "Sets the behaviour of a NPC",
            modifiers = { "behaviour", "ai" },
            min = 2,
            max = -1)
    public void behaviour(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Iterable<String> files = Splitter.on(',').split(args.getJoinedStrings(1, ','));
        npc.getTrait(Behaviour.class).addScripts(files);
        sender.sendMessage(ChatColor.GREEN + "Behaviours added.");
    }

    @Command(
            aliases = { "npc" },
            usage = "controllable",
            desc = "Toggles whether the NPC can be ridden and controlled",
            modifiers = { "controllable" },
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
            usage = "create [name] ((-b) --type (type) --char (char) --behaviour (behaviour))",
            desc = "Create a new NPC",
            flags = "b",
            modifiers = { "create" },
            min = 2,
            max = 5,
            permission = "npc.create")
    @Requirements
    public void create(CommandContext args, final Player player, NPC npc) {
        String name = args.getString(1);
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

        if (args.hasValueFlag("behaviour")) {
            npc.getTrait(Behaviour.class).addScripts(Splitter.on(",").split(args.getFlag("behaviour")));
            msg += " with the specified behaviours";
        }

        msg += ".";

        // Initialize necessary traits
        if (!Setting.SERVER_OWNS_NPCS.asBoolean())
            npc.getTrait(Owner.class).setOwner(player.getName());
        npc.getTrait(MobType.class).setType(type);

        npc.spawn(player.getLocation());

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
            usage = "moveto",
            desc = "Teleports a NPC to a given location",
            modifiers = "moveto",
            min = 1,
            max = 1,
            permission = "npc.moveto")
    public void moveto(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        Location current = npc.getBukkitEntity().getLocation();
        Location to = current.clone();
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

        npc.getBukkitEntity().teleport(to, TeleportCause.COMMAND);
        Messaging.send(sender, StringHelper.wrap(npc.getName()) + " was teleported to " + to + ".");
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0)
    public void npc(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(sender, "    <a>ID: <e>" + npc.getId());
        Messaging.send(sender, "    <a>Type: <e>" + npc.getTrait(MobType.class).getType());
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
            usage = "profession [profession]",
            desc = "Set a NPC's profession",
            modifiers = { "profession" },
            min = 2,
            max = 2,
            permission = "npc.profession")
    @Requirements(selected = true, ownership = true, types = { EntityType.VILLAGER })
    public void profession(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String profession = args.getString(1);
        try {
            npc.getTrait(VillagerProfession.class)
                    .setProfession(Profession.valueOf(profession.toUpperCase()));
            Messaging.send(sender, StringHelper.wrap(npc.getName()) + " is now the profession "
                    + StringHelper.wrap(profession.toUpperCase()) + ".");
        } catch (IllegalArgumentException ex) {
            throw new CommandException("'" + profession + "' is not a valid profession.");
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "remove (all)",
            desc = "Remove a NPC",
            modifiers = { "remove" },
            min = 1,
            max = 2)
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
            max = 2,
            permission = "npc.rename")
    public void rename(CommandContext args, CommandSender sender, NPC npc) {
        String oldName = npc.getName();
        String newName = args.getString(1);
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
            usage = "select [id]",
            desc = "Select a NPC with the given ID",
            modifiers = { "select" },
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
            "tphere", "move" }, min = 1, max = 1, permission = "npc.tphere")
    public void tphere(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(CurrentLocation.class).getLocation());
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
    }

    @Command(
            aliases = { "npc" },
            usage = "trait [trait name]",
            desc = "Adds a trait to the NPC",
            modifiers = { "trait" },
            min = 2,
            max = 2,
            flags = "r",
            permission = "npc.trait")
    public void trait(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String traitName = args.getString(1);
        if (!sender.hasPermission("citizens.npc.trait." + traitName))
            throw new NoPermissionsException();
        if (args.hasFlag('r')) {
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(args.getString(1));
            if (clazz == null)
                throw new CommandException("Trait not found.");
            if (!npc.hasTrait(clazz))
                throw new CommandException("The NPC doesn't have that trait.");
            npc.removeTrait(clazz);
            Messaging.sendF(sender, ChatColor.GREEN + "Trait %s removed successfully.",
                    StringHelper.wrap(traitName));
            return;
        }
        Trait trait = CitizensAPI.getTraitFactory().getTrait(traitName);
        if (trait == null)
            throw new CommandException("Trait not found.");
        npc.addTrait(trait);
        Messaging.sendF(sender, ChatColor.GREEN + "Trait %s added successfully.",
                StringHelper.wrap(traitName));
    }
}