package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.npc.CitizensCharacterManager;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final CitizensCharacterManager characterManager;
    private final CitizensNPCManager npcManager;

    public NPCCommands(Citizens plugin) {
        npcManager = plugin.getNPCManager();
        characterManager = plugin.getCharacterManager();
    }

    @Command(
             aliases = { "npc" },
             usage = "character [character]",
             desc = "Set the character of an NPC",
             modifiers = { "character" },
             min = 2,
             max = 2)
    public void character(CommandContext args, Player player, NPC npc) throws CommandException {
        String name = args.getString(1).toLowerCase();
        Character character = characterManager.getCharacter(name);
        if (character == null)
            throw new CommandException("The character '" + args.getString(1) + "' does not exist.");
        if (npc.getCharacter() != null && npc.getCharacter().getName().equalsIgnoreCase(character.getName()))
            throw new CommandException("The NPC already has the character '" + name + "'.");
        if (!player.hasPermission("citizens.npc.character." + character.getName())
                && !player.hasPermission("citizens.npc.character.*") && !player.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        Messaging.send(player, StringHelper.wrap(npc.getName() + "'s") + " character is now '"
                + StringHelper.wrap(name) + "'.");
        npc.setCharacter(character);
    }

    @Command(
             aliases = { "npc" },
             usage = "create [name] (--type (type) --char (char))",
             desc = "Create a new NPC",
             modifiers = { "create" },
             min = 2,
             max = 5,
             permission = "npc.create")
    @Requirements
    public void create(CommandContext args, Player player, NPC npc) {
        String name = args.getString(1);
        if (name.length() > 16) {
            Messaging.sendError(player, "NPC names cannot be longer than 16 characters. The name has been shortened.");
            name = name.substring(0, 15);
        }
        EntityType type = EntityType.PLAYER;
        if (args.hasValueFlag("type"))
            try {
                type = EntityType.valueOf(args.getFlag("type").toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException ex) {
                Messaging.sendError(player, "'" + args.getFlag("type")
                        + "' is not a valid mob type. Using default NPC.");
            }
        NPC create = npcManager.createNPC(type, name);
        String msg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
        if (args.hasValueFlag("char")) {
            String character = args.getFlag("char").toLowerCase();
            if (characterManager.getCharacter(character) == null) {
                Messaging.sendError(player, "'" + args.getFlag("char") + "' is not a valid character.");
                return;
            } else {
                Character set = characterManager.getCharacter(character);
                if (!set.getValidTypes().contains(type)) {
                    Messaging.sendError(player, "The character '" + set.getName() + "' cannot be given the mob type '"
                            + type.name().toLowerCase() + "'.");
                    create.remove();
                    return;
                }
                create.setCharacter(characterManager.getCharacter(character));
                msg += " with the character " + StringHelper.wrap(character);
            }
        }
        msg += " at your location.";

        // Set the owner
        create.getTrait(Owner.class).setOwner(player.getName());

        // Set the mob type
        create.getTrait(MobType.class).setType(type.toString());

        create.spawn(player.getLocation());
        npcManager.selectNPC(player, create);
        Messaging.send(player, msg);
    }

    @Command(
             aliases = { "npc" },
             usage = "despawn",
             desc = "Despawn an NPC",
             modifiers = { "despawn" },
             min = 1,
             max = 1,
             permission = "npc.despawn")
    public void despawn(CommandContext args, Player player, NPC npc) {
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn();
        Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(npc.getName()) + ".");
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
    public void list(CommandContext args, Player player, NPC npc) throws CommandException {
        List<NPC> npcs = new ArrayList<NPC>();

        if (args.hasFlag('a')) {
            for (NPC add : npcManager)
                npcs.add(add);
        } else if (args.getValueFlags().size() == 0 && args.argsLength() == 1 || args.argsLength() == 2) {
            for (NPC add : npcManager)
                if (add.getTrait(Owner.class).getOwner().equalsIgnoreCase(player.getName()))
                    npcs.add(add);
        } else {
            if (args.hasValueFlag("owner")) {
                String name = args.getFlag("owner");
                for (NPC add : npcManager)
                    if (add.getTrait(Owner.class).getOwner().equalsIgnoreCase(name))
                        npcs.add(add);
            }

            if (args.hasValueFlag("type")) {
                String type = args.getFlag("type");
                try {
                    EntityType.valueOf(type.toUpperCase().replace('-', '_'));
                } catch (IllegalArgumentException ex) {
                    throw new CommandException("'" + type + "' is not a valid mob type.");
                }

                for (NPC add : npcManager)
                    if (!npcs.contains(add) && add.getTrait(MobType.class).getType().equalsIgnoreCase(type))
                        npcs.add(add);
            }

            if (args.hasValueFlag("char")) {
                String character = args.getFlag("char");
                if (characterManager.getCharacter(character) == null)
                    throw new CommandException("'" + character + "' is not a valid character.");

                for (NPC add : npcManager.getNPCs(characterManager.getCharacter(character).getClass()))
                    if (!npcs.contains(add) && add.getCharacter() != null
                            && add.getCharacter().getName().equals(character.toLowerCase()))
                        npcs.add(add);
            }
        }

        Paginator paginator = new Paginator();
        paginator.setHeaderText("NPCs");
        paginator.addLine("<e>Key: <a>ID  <b>Name");
        for (int i = 0; i < npcs.size(); i += 2) {// 0,2,4,6,etc, size=3
            String line = "<a>" + npcs.get(i).getId() + "<b>  " + npcs.get(i).getName();
            if (npcs.size() >= i + 2)
                line += "      " + "<a>" + npcs.get(i + 1).getId() + "<b>  " + npcs.get(i + 1).getName();
            paginator.addLine(line);
        }

        int page = 1;
        try {
            page = args.getInteger(1);
        } catch (Exception ex) {
        }
        if (!paginator.sendPage(player, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }

    @Command(
             aliases = { "npc" },
             usage = "lookclose",
             desc = "Toggle whether an NPC will look when a player is near",
             modifiers = { "lookclose", "look", "rotate" },
             min = 1,
             max = 1,
             permission = "npc.lookclose")
    public void lookClose(CommandContext args, Player player, NPC npc) {
        String msg = StringHelper.wrap(npc.getName()) + " will "
                + (npc.getTrait(LookClose.class).toggle() ? "now rotate" : "no longer rotate");
        Messaging.send(player, msg += " when a player is nearby.");
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0)
    public void npc(CommandContext args, Player player, NPC npc) {
        Messaging.send(player, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(player, "    <a>ID: <e>" + npc.getId());
        Messaging.send(player, "    <a>Character: <e>"
                + (npc.getCharacter() != null ? npc.getCharacter().getName() : "None"));
        Messaging.send(player, "    <a>Type: <e>" + npc.getTrait(MobType.class).getType());
    }

    @Command(
             aliases = { "npc" },
             usage = "owner [name]",
             desc = "Set the owner of an NPC",
             modifiers = { "owner" },
             min = 2,
             max = 2,
             permission = "npc.owner")
    public void owner(CommandContext args, Player player, NPC npc) throws CommandException {
        String name = args.getString(1);
        if (npc.getTrait(Owner.class).getOwner().equals(name))
            throw new CommandException("'" + name + "' is already the owner of " + npc.getName() + ".");
        npc.getTrait(Owner.class).setOwner(name);
        Messaging.send(player, StringHelper.wrap(name) + " is now the owner of " + StringHelper.wrap(npc.getName())
                + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "remove (all)",
             desc = "Remove an NPC",
             modifiers = { "remove" },
             min = 1,
             max = 2)
    @Requirements
    public void remove(CommandContext args, Player player, NPC npc) throws CommandException {
        if (args.argsLength() == 2) {
            if (!args.getString(1).equals("all"))
                throw new CommandException("Incorrect syntax. /npc remove (all)");
            if (!player.hasPermission("citizens.npc.remove.all") && !player.hasPermission("citizens.admin"))
                throw new NoPermissionsException();
            npcManager.removeAll();
            Messaging.send(player, "<a>You permanently removed all NPCs.");
            return;
        }
        if (npc == null)
            throw new CommandException("You must have an NPC selected to execute that command.");
        if (!npc.getTrait(Owner.class).getOwner().equals(player.getName()) && !player.hasPermission("citizens.admin"))
            throw new CommandException("You must be the owner of this NPC to execute that command.");
        if (!player.hasPermission("citizens.npc.remove") && !player.hasPermission("citizens.admin"))
            throw new NoPermissionsException();
        npc.remove();
        Messaging.send(player, "<a>You permanently removed " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "rename [name]",
             desc = "Rename an NPC",
             modifiers = { "rename" },
             min = 2,
             max = 2,
             permission = "npc.rename")
    public void rename(CommandContext args, Player player, NPC npc) {
        String oldName = npc.getName();
        String newName = args.getString(1);
        if (newName.length() > 16) {
            Messaging.sendError(player, "NPC names cannot be longer than 16 characters. The name has been shortened.");
            newName = newName.substring(0, 15);
        }
        npc.setName(newName);
        Messaging.send(player, ChatColor.GREEN + "You renamed " + StringHelper.wrap(oldName) + " to "
                + StringHelper.wrap(newName) + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "select [id]",
             desc = "Select an NPC with the given ID",
             modifiers = { "select" },
             min = 2,
             max = 2,
             permission = "npc.select")
    @Requirements(ownership = true)
    public void select(CommandContext args, Player player, NPC npc) throws CommandException {
        NPC toSelect = npcManager.getNPC(args.getInteger(1));
        if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn())
            throw new CommandException("No NPC with the ID '" + args.getInteger(1) + "' is spawned.");
        if (npc != null && toSelect.getId() == npc.getId())
            throw new CommandException("You already have that NPC selected.");
        npcManager.selectNPC(player, toSelect);
        Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), toSelect);
    }

    @Command(
             aliases = { "npc" },
             usage = "spawn [id]",
             desc = "Spawn an existing NPC",
             modifiers = { "spawn" },
             min = 2,
             max = 2,
             permission = "npc.spawn")
    @Requirements
    public void spawn(CommandContext args, Player player, NPC npc) throws CommandException {
        NPC respawn = npcManager.getNPC(args.getInteger(1));
        if (respawn == null)
            throw new CommandException("No NPC with the ID '" + args.getInteger(1) + "' exists.");

        if (!respawn.getTrait(Owner.class).getOwner().equals(player.getName()))
            throw new CommandException("You must be the owner of this NPC to execute that command.");

        if (respawn.spawn(player.getLocation())) {
            npcManager.selectNPC(player, respawn);
            Messaging.send(player, ChatColor.GREEN + "You respawned " + StringHelper.wrap(respawn.getName())
                    + " at your location.");
        } else
            throw new CommandException(respawn.getName() + " is already spawned at another location."
                    + " Use '/npc tphere' to teleport the NPC to your location.");
    }

    @Command(
             aliases = { "npc" },
             usage = "tp",
             desc = "Teleport to an NPC",
             modifiers = { "tp", "teleport" },
             min = 1,
             max = 1,
             permission = "npc.tp")
    public void tp(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        player.teleport(npc.getBukkitEntity(), TeleportCause.COMMAND);
        Messaging.send(player, ChatColor.GREEN + "You teleported to " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "tphere",
             desc = "Teleport an NPC to your location",
             modifiers = { "tphere" },
             min = 1,
             max = 1,
             permission = "npc.tphere")
    public void tphere(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        npc.getTrait(SpawnLocation.class).setLocation(npc.getBukkitEntity().getLocation());
        Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
    }
}