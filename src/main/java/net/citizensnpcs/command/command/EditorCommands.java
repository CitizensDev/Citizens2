package net.citizensnpcs.command.command;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Requirements(selected = true, ownership = true)
public class EditorCommands {

    @Command(aliases = { "npc" }, usage = "equip", desc = "Toggle the equipment editor", modifiers = { "equip" }, min = 1, max = 1, permission = "npc.edit.equip")
    @Requirements(selected = true, ownership = true, types = { EntityType.ENDERMAN, EntityType.PLAYER, EntityType.PIG,
            EntityType.SHEEP })
    public void equip(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, new EquipmentEditor(player, npc));
    }

    @Command(aliases = { "npc" }, usage = "path", desc = "Toggle the waypoint editor", modifiers = { "path" }, min = 1, max = 1, permission = "npc.edit.path")
    public void path(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Waypoints.class).getEditor(player));
    }

    @Command(aliases = { "npc" }, usage = "text", desc = "Toggle the text editor", modifiers = { "text" }, min = 1, max = 1, permission = "npc.edit.text")
    public void text(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Text.class).getEditor(player));
    }
}