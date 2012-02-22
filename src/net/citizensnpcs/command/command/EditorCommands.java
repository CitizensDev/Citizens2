package net.citizensnpcs.command.command;

import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.util.Messaging;

public class EditorCommands {

    public EditorCommands(Citizens plugin) {
    }

    @Command(
             aliases = { "npc" },
             usage = "equip",
             desc = "Toggle equipment editor",
             modifiers = { "equip" },
             min = 1,
             max = 1,
             permission = "npc.equip")
    public void toggleEquipEditor(CommandContext args, Player player, NPC npc) {
        if (!(npc instanceof Player)) {
            Messaging.sendError(player, "The NPC must be a human to equip armor and items.");
            return;
        }
    }

    @Command(
             aliases = { "npc" },
             usage = "path",
             desc = "Toggle path editor",
             modifiers = { "path" },
             min = 1,
             max = 1,
             permission = "npc.path")
    public void togglePathEditor(CommandContext args, Player player, NPC npc) {
        // TODO
    }

    @Command(
             aliases = { "npc" },
             usage = "text",
             desc = "Toggle text editor",
             modifiers = { "text" },
             min = 1,
             max = 1,
             permission = "npc.text")
    public void toggleTextEditor(CommandContext args, Player player, NPC npc) {
        // TODO
    }
}