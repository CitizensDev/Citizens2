package net.citizensnpcs.command.command;

import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

@Requirements(selected = true, ownership = true)
public class EditorCommands {

    @Command(
            aliases = { "npc" },
            usage = "equip",
            desc = "Toggle the equipment editor",
            modifiers = { "equip" },
            min = 1,
            max = 1,
            permission = "npc.edit.equip")
    @Requirements(selected = true, ownership = true, types = { MobType.ENDERMAN, MobType.PLAYER, MobType.PIG,
            MobType.SHEEP })
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
            permission = "npc.edit.path")
    @Requirements(selected = true, ownership = true, excludedTypes = { MobType.ENDER_DRAGON, MobType.SQUID,
            MobType.GHAST, MobType.BLAZE })
    public void path(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getAttachment(Waypoints.class).getEditor(player));
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
        Editor.enterOrLeave(player, npc.getAttachment(Text.class).getEditor(player));
    }
}