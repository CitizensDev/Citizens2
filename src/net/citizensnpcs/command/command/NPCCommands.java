package net.citizensnpcs.command.command;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.DefaultInstanceFactory;
import net.citizensnpcs.api.npc.trait.trait.MobType;
import net.citizensnpcs.api.npc.trait.trait.Owner;
import net.citizensnpcs.api.npc.trait.trait.Spawned;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.command.annotation.Permission;
import net.citizensnpcs.command.annotation.Requirements;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Requirements(selected = true, ownership = true)
public class NPCCommands {
    private final CitizensNPCManager npcManager;
    private final DefaultInstanceFactory<Character> characterManager;

    public NPCCommands(CitizensNPCManager npcManager, DefaultInstanceFactory<Character> characterManager) {
        this.npcManager = npcManager;
        this.characterManager = characterManager;
    }

    @Command(
             aliases = { "npc" },
             usage = "create [name] [type] (character)",
             desc = "Create a new NPC",
             modifiers = { "create" },
             min = 3,
             max = 4)
    @Permission("npc.create")
    @Requirements
    public void createNPC(CommandContext args, Player player, NPC npc) {
        CreatureType type = CreatureType.MONSTER; // Default human type
        try {
            type = CreatureType.valueOf(args.getString(2).toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            Messaging.sendError(player, "'" + args.getString(2) + "' is not a valid mob type. Using default NPC.");
        }

        String name = args.getString(1);
        if (args.getString(1).length() > 16) {
            Messaging.sendError(player, "NPC names cannot be longer than 16 characters. The name has been shortened.");
            name = name.substring(0, 15);
        }
        NPC create = npcManager.createNPC(type, name);
        String successMsg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
        boolean success = true;
        if (args.argsLength() == 4) {
            if (characterManager.getInstance(args.getString(3), create) == null) {
                Messaging.sendError(player,
                        "The character '" + args.getString(3) + "' does not exist. " + create.getName()
                                + " was created at your location without a character.");
                success = false;
            } else {
                create.setCharacter(characterManager.getInstance(args.getString(3), create));
                successMsg += " with the character " + StringHelper.wrap(args.getString(3));
            }
        }
        successMsg += " at your location.";

        // Set the owner
        create.addTrait(new Owner(player.getName()));

        // Set the mob type
        create.addTrait(new MobType(type == CreatureType.MONSTER ? "DEFAULT" : type.toString()));

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
             max = 1)
    @Permission("npc.despawn")
    public void despawnNPC(CommandContext args, Player player, NPC npc) {
        npc.getTrait(Spawned.class).setSpawned(false);
        npc.despawn();
        Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(
             aliases = { "npc" },
             usage = "select [id]",
             desc = "Select an NPC",
             modifiers = { "select" },
             min = 2,
             max = 2)
    @Permission("npc.select")
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
             usage = "spawn [id]",
             desc = "Spawn an existing NPC",
             modifiers = { "spawn" },
             min = 2,
             max = 2)
    @Permission("npc.spawn")
    @Requirements
    public void spawnNPC(CommandContext args, Player player, NPC npc) {
        CitizensNPC respawn = (CitizensNPC) npcManager.getNPC(args.getInteger(1));
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
        } else
            Messaging
                    .sendError(
                            player,
                            respawn.getName()
                                    + " is already spawned at another location. Use '/npc tphere' to teleport the NPC to your location.");
    }

    @Command(
             aliases = { "npc" },
             usage = "tphere",
             desc = "Teleport an NPC to your location",
             modifiers = { "tphere" },
             min = 1,
             max = 1)
    @Permission("npc.tphere")
    public void teleportNPCToPlayer(CommandContext args, Player player, NPC npc) {
        npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
        Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
    }

    @Command(
             aliases = { "npc" },
             usage = "tp",
             desc = "Teleport to an NPC",
             modifiers = { "tp", "teleport" },
             min = 1,
             max = 1)
    @Permission("npc.tp")
    public void teleportToNPC(CommandContext args, Player player, NPC npc) {
        player.teleport(npc.getBukkitEntity(), TeleportCause.COMMAND);
        Messaging.send(player, ChatColor.GREEN + "You teleported to " + StringHelper.wrap(npc.getName()) + ".");
    }

    @Command(aliases = { "npc" }, usage = "lookclose", desc = "Toggle an NPC's look-close state", modifiers = {
            "lookclose", "look", "rotate" }, min = 1, max = 1)
    @Permission("npc.look-close")
    public void toggleNPCLookClose(CommandContext args, Player player, NPC npc) {
        npc.getTrait(LookClose.class).setLookClose(!npc.getTrait(LookClose.class).shouldLookClose());
        String msg = StringHelper.wrap(npc.getName()) + " will ";
        if (npc.getTrait(LookClose.class).shouldLookClose())
            msg += "now rotate";
        else
            msg += "no longer";
        Messaging.send(player, msg += " when a player is nearby.");
    }
}