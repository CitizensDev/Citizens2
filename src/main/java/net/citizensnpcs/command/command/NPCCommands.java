package net.citizensnpcs.command.command;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Character;
import net.citizensnpcs.api.trait.InstanceFactory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final CitizensNPCManager npcManager;
    private final InstanceFactory<Character> characterManager;

    public NPCCommands(Citizens plugin) {
        npcManager = plugin.getNPCManager();
        characterManager = plugin.getCharacterManager();
    }

    @Command(aliases = { "npc" }, desc = "Show basic NPC information", max = 0)
    public void showInfo(CommandContext args, Player player, NPC npc) {
        Messaging.send(player, StringHelper.wrapHeader(npc.getName()));
        Messaging.send(player, "    <a>ID: <e>" + npc.getId());
        Messaging.send(player, "    <a>Character: <e>"
                + (npc.getCharacter() != null ? npc.getCharacter().getName() : "None"));
        Messaging.send(player, "    <a>Type: <e>" + npc.getTrait(MobType.class).getType());
    }

    @Command(
             aliases = { "npc" },
             usage = "create [name] --type (type) --char (character)",
             desc = "Create a new NPC",
             modifiers = { "create" },
             min = 2,
             max = 5,
             permission = "npc.create")
    @Requirements
    public void createNPC(CommandContext args, Player player, NPC npc) {
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
        String successMsg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
        boolean success = true;
        if (args.hasValueFlag("char")) {
            String character = args.getFlag("char").toLowerCase();
            if (characterManager.getInstance(character, create) == null) {
                Messaging.sendError(player, "The character '" + args.getFlag("char") + "' does not exist. "
                        + create.getName() + " was created at your location without a character.");
                success = false;
            } else {
                create.setCharacter(characterManager.getInstance(character, create));
                successMsg += " with the character " + StringHelper.wrap(character);
            }
        }
        successMsg += " at your location.";

        // Set the owner
        create.addTrait(new Owner(player.getName()));

        // Set the mob type
        create.addTrait(new MobType(type.toString()));

        create.spawn(player.getLocation());
        npcManager.selectNPC(player, create);
        if (success)
            Messaging.send(player, successMsg);
    }

    @Command(
             aliases = { "npc" },
             usage = "despawn",
             desc = "Despawn an NPC",
             modifiers = { "despawn" },
             min = 1,
             max = 1,
             permission = "npc.despawn")
    public void despawnNPC(CommandContext args, Player player, NPC npc) {
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn();
        Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "remove (all)",
             desc = "Remove an NPC",
             modifiers = { "remove" },
             min = 1,
             max = 2)
    @Requirements
    public void removeNPC(CommandContext args, Player player, NPC npc) {
        if (args.argsLength() == 2) {
            if (!player.hasPermission("citizens.npc.remove.all") && !player.hasPermission("citizens.admin")) {
                Messaging.sendError(player, "You don't have permission to execute that command.");
                return;
            }
            npcManager.removeAll();
            Messaging.send(player, "<a>You permanently removed all NPCs.");
            return;
        }
        if (npc == null) {
            Messaging.sendError(player, "You must have an NPC selected to execute that command.");
            return;
        }
        if (!npc.getTrait(Owner.class).getOwner().equals(player.getName()) && !player.hasPermission("citizens.admin")) {
            Messaging.sendError(player, "You must be the owner of this NPC to execute that command.");
            return;
        }
        if (!player.hasPermission("citizens.npc.remove") && !player.hasPermission("citizens.admin")) {
            Messaging.sendError(player, "You don't have permission to execute that command.");
            return;
        }
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
    public void renameNPC(CommandContext args, Player player, NPC npc) {
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
             desc = "Selects an NPC with the given ID",
             modifiers = { "select" },
             min = 2,
             max = 2,
             permission = "npc.select")
    @Requirements(ownership = true)
    public void selectNPC(CommandContext args, Player player, NPC npc) {
        NPC toSelect = npcManager.getNPC(args.getInteger(1));
        if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn()) {
            Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' is spawned.");
            return;
        }
        if (npc != null && toSelect.getId() == npc.getId()) {
            Messaging.sendError(player, "You already have that NPC selected.");
            return;
        }
        npcManager.selectNPC(player, toSelect);
        Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), toSelect);
    }

    @Command(
             aliases = { "npc" },
             usage = "character [character]",
             desc = "Sets the character of an NPC",
             modifiers = { "character" },
             min = 2,
             max = 2)
    public void setNPCCharacter(CommandContext args, Player player, NPC npc) {
        String name = args.getString(1).toLowerCase();
        Character character = characterManager.getInstance(name, npc);
        if (character == null) {
            Messaging.sendError(player, "The character '" + args.getString(1) + "' does not exist.");
            return;
        }
        if (npc.getCharacter() != null && npc.getCharacter().getName().equalsIgnoreCase(character.getName())) {
            Messaging.sendError(player, "The NPC already has the character '" + name + "'.");
            return;
        }
        if (!player.hasPermission("citizens.npc.character." + character.getName())
                && !player.hasPermission("citizens.npc.character.*") && !player.hasPermission("citizens.admin")) {
            Messaging.sendError(player, "You don't have permission to execute that command.");
            return;
        }
        Messaging.send(player, StringHelper.wrap(npc.getName() + "'s") + " character is now '"
                + StringHelper.wrap(name) + "'.");
        npc.setCharacter(character);
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
    public void spawnNPC(CommandContext args, Player player, NPC npc) {
        NPC respawn = npcManager.getNPC(args.getInteger(1));
        if (respawn == null) {
            Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' exists.");
            return;
        }

        if (!respawn.getTrait(Owner.class).getOwner().equals(player.getName())) {
            Messaging.sendError(player, "You must be the owner of this NPC to execute that command.");
            return;
        }

        if (respawn.spawn(player.getLocation())) {
            npcManager.selectNPC(player, respawn);
            Messaging.send(player, ChatColor.GREEN + "You respawned " + StringHelper.wrap(respawn.getName())
                    + " at your location.");
        } else {
            Messaging.sendError(player, respawn.getName() + " is already spawned at another location."
                    + " Use '/npc tphere' to teleport the NPC to your location.");
        }
    }

    @Command(
             aliases = { "npc" },
             usage = "tphere",
             desc = "Teleport an NPC to your location",
             modifiers = { "tphere" },
             min = 1,
             max = 1,
             permission = "npc.tphere")
    public void teleportNPCToPlayer(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        npc.getTrait(SpawnLocation.class).setLocation(npc.getBukkitEntity().getLocation());
        Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
    }

    @Command(
             aliases = { "npc" },
             usage = "tp",
             desc = "Teleport to an NPC",
             modifiers = { "tp", "teleport" },
             min = 1,
             max = 1,
             permission = "npc.tp")
    public void teleportToNPC(CommandContext args, Player player, NPC npc) {
        // Spawn the NPC if it isn't spawned to prevent NPEs
        if (!npc.isSpawned())
            npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        player.teleport(npc.getBukkitEntity(), TeleportCause.COMMAND);
        Messaging.send(player, ChatColor.GREEN + "You teleported to " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(aliases = { "npc" }, usage = "lookclose", desc = "Toggle an NPC's look-close state", modifiers = {
            "lookclose", "look", "rotate" }, min = 1, max = 1, permission = "npc.lookclose")
    public void toggleNPCLookClose(CommandContext args, Player player, NPC npc) {
        LookClose trait = npc.getTrait(LookClose.class);
        trait.toggle();
        String msg = StringHelper.wrap(npc.getName()) + " will "
                + (trait.shouldLookClose() ? "now rotate" : "no longer rotate");
        Messaging.send(player, msg += " when a player is nearby.");
    }
}