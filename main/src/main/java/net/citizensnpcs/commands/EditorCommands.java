package net.citizensnpcs.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.CopierEditor;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Requirements(selected = true, ownership = true)
public class EditorCommands {
    @Command(
            aliases = { "npc" },
            usage = "copier",
            desc = "Toggle the NPC copier",
            modifiers = { "copier" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.copier")
    public void copier(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, new CopierEditor(player, npc));
    }

    @Command(
            aliases = { "npc" },
            usage = "equip",
            desc = "Toggle the equipment editor",
            modifiers = { "equip" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.equip")
    public void equip(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, new EquipmentEditor(player, npc));
    }

    @Command(
            aliases = { "npc" },
            usage = "path",
            desc = "Toggle the waypoint editor",
            modifiers = { "path" },
            min = 1,
            max = 1,
            flags = "*",
            permission = "citizens.npc.edit.path")
    @Requirements(selected = true, ownership = true)
    public void path(CommandContext args, CommandSender player, NPC npc) {
        Editor editor = npc.getTrait(Waypoints.class).getEditor(player, args);
        if (editor == null)
            return;
        Editor.enterOrLeave((Player) player, editor);
    }

    @Command(
            aliases = { "npc" },
            usage = "text",
            desc = "Toggle the text editor",
            modifiers = { "text" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.text")
    public void text(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Text.class).getEditor(player));
    }
}
