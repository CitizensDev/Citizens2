package net.citizensnpcs.command.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.command.annotation.Permission;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

// TODO add requirements
public class NPCCommands {
    private final Citizens plugin;

    public NPCCommands(Citizens plugin) {
        this.plugin = plugin;
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
        CitizensNPC create = (CitizensNPC) plugin.getNPCManager().createNPC(args.getString(1));
        String msg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
        if (args.argsLength() == 3 && plugin.getCharacterManager().getInstance(args.getString(2)) != null) {
            create.setCharacter(plugin.getCharacterManager().getInstance(args.getString(2)));
            msg += " with the character " + StringHelper.wrap(args.getString(2));
        }
        msg += " at your location.";

        create.spawn(player.getLocation());
        plugin.getNPCManager().selectNPC(player, create);
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
        CitizensNPC respawn = (CitizensNPC) plugin.getNPCManager().getNPC(args.getInteger(1));
        if (respawn == null) {
            Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' exists.");
            return;
        }

        respawn.spawn(player.getLocation());
        plugin.getNPCManager().selectNPC(player, respawn);
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
        CitizensNPC despawn = (CitizensNPC) plugin.getNPCManager().getSelectedNPC(player);
        despawn.despawn();
        plugin.getNPCManager().deselectNPC(player);
        Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(despawn.getName()) + ".");
    }
}