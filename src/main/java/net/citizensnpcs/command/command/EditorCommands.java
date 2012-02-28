package net.citizensnpcs.command.command;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.editor.TextEditor;
import net.citizensnpcs.trait.waypoint.Waypoints;

@Requirements(selected = true, ownership = true)
public class EditorCommands {
    private final Citizens plugin;

    public EditorCommands(Citizens plugin) {
        this.plugin = plugin;
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
        Editor.enterOrLeave(player, new EquipmentEditor(plugin, player, npc));
    }

    @Command(
             aliases = { "npc" },
             usage = "path",
             desc = "Toggle the waypoint editor",
             modifiers = { "path" },
             min = 1,
             max = 1,
             permission = "npc.edit.path")
    public void path(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Waypoints.class).getEditor(player));
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
        Editor.enterOrLeave(player, new TextEditor());
    }
}