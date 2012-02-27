package net.citizensnpcs.command.command;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;

@Requirements(selected = true, ownership = true)
public class EditorCommands {

    public EditorCommands(Citizens plugin) {
    }

    @Command(
             aliases = { "npc" },
             usage = "equip",
             desc = "Toggle the equipment editor",
             modifiers = { "equip" },
             min = 1,
             max = 1,
             permission = "npc.edit.equip")
    @Requirements(selected = true, ownership = true, type = EntityType.PLAYER)
    public void equip(CommandContext args, Player player, NPC npc) {
    }

    @Command(
             aliases = { "npc" },
             usage = "path",
             desc = "Toggle the waypoint editor",
             modifiers = { "path", "waypoints" },
             min = 1,
             max = 1,
             permission = "npc.edit.path")
    public void path(CommandContext args, Player player, NPC npc) {
    }

    @Command(
             aliases = { "npc" },
             usage = "text",
             desc = "Toggle the text editor",
             modifiers = { "text" },
             min = 1,
             max = 1,
             permission = "npc.edit.text")
    public void text(CommandContext args, Player player, NPC npc) {
    }
}