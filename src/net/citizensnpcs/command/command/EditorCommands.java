package net.citizensnpcs.command.command;

import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;

public class EditorCommands {

    public EditorCommands(Citizens plugin) {
    }

    @Command(
             aliases = { "npc" },
             usage = "edit (editor)",
             desc = "Toggle an NPC editor",
             modifiers = { "edit" },
             min = 2,
             max = 2)
    public void toggleEquipEditor(CommandContext args, Player player, NPC npc) {
    }
}