package net.citizensnpcs.command.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.DefaultInstanceFactory;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.command.annotation.Permission;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

// TODO add requirements
public class NPCCommands {
    private final CitizensNPCManager npcManager;
    private final DefaultInstanceFactory<Character> characterManager;

    public NPCCommands(CitizensNPCManager npcManager, DefaultInstanceFactory<Character> characterManager) {
        this.npcManager = npcManager;
        this.characterManager = characterManager;
    }

    @Command(
             aliases = { "npc" },
             usage = "create [name] (character)",
             desc = "Create a new NPC",
             modifiers = { "create" },
             min = 2,
             max = 3)
    @Permission("npc.create")
    public void createNPC(CommandContext args, Player player, NPC npc) {
        CitizensNPC create = (CitizensNPC) npcManager.createNPC(args.getString(1));
        String msg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
        if (args.argsLength() == 3 && characterManager.getInstance(args.getString(2)) != null) {
            create.setCharacter(characterManager.getInstance(args.getString(2)));
            msg += " with the character " + StringHelper.wrap(args.getString(2));
        }
        msg += " at your location.";

        create.spawn(player.getLocation());
        npcManager.selectNPC(player, create);
        Messaging.send(player, msg);
    }

    @Command(
             aliases = { "npc" },
             usage = "spawn [id]",
             desc = "Spawn an existing NPC",
             modifiers = { "spawn" },
             min = 2,
             max = 2)
    @Permission("npc.spawn")
    public void spawnNPC(CommandContext args, Player player, NPC npc) {
        CitizensNPC respawn = (CitizensNPC) npcManager.getNPC(args.getInteger(1));
        if (respawn == null) {
            Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' exists.");
            return;
        }

        respawn.spawn(player.getLocation());
        npcManager.selectNPC(player, respawn);
        Messaging.send(player, ChatColor.GREEN + "You respawned " + StringHelper.wrap(respawn.getName())
                + " at your location.");
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
        CitizensNPC despawn = (CitizensNPC) npcManager.getSelectedNPC(player);
        despawn.despawn();
        npcManager.deselectNPC(player);
        Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(despawn.getName()) + ".");
    }
}