package net.citizensnpcs.command.command;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.command.annotation.Permission;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

// TODO add requirements
public class NPCCommands {

    @Command(
             aliases = { "npc" },
             usage = "spawn [name]",
             desc = "Spawn an NPC",
             modifiers = { "spawn", "create" },
             min = 2,
             max = 2)
    @Permission("npc.spawn")
    public static void spawnNPC(CommandContext args, Player player, NPC npc) {
        CitizensNPC create = (CitizensNPC) CitizensAPI.getNPCManager().createNPC(args.getString(1));
        create.spawn(player.getLocation());
        create.save();
    }

    @Command(
             aliases = { "npc" },
             usage = "despawn",
             desc = "Despawn an NPC",
             modifiers = { "despawn" },
             min = 1,
             max = 1)
    @Permission("npc.despawn")
    public static void despawnNPC(CommandContext args, Player player, NPC npc) {
        CitizensNPC despawn = (CitizensNPC) ((CitizensNPCManager) CitizensAPI.getNPCManager()).getSelectedNPC(player);
        despawn.despawn();
    }
}