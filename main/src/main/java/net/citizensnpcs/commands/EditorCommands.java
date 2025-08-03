package net.citizensnpcs.commands;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.CopierEditor;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

@Requirements(selected = true, ownership = true)
public class EditorCommands {
    @Command(
            aliases = { "npc" },
            usage = "copier",
            desc = "",
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
            desc = "",
            modifiers = { "equip" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.equip")
    public void equip(CommandContext args, Player player, NPC npc) throws CommandException {
        if (!npc.isSpawned())
            throw new CommandException("NPC must be spawned");

        Editor.enterOrLeave(player, new EquipmentEditor(player, npc));
    }

    @Command(
            aliases = { "npc" },
            usage = "path",
            desc = "",
            modifiers = { "path" },
            min = 1,
            flags = "*",
            permission = "citizens.npc.edit.path")
    @Requirements(selected = true, ownership = true)
    public void path(CommandContext args, Player player, NPC npc) {
        Editor editor = npc.getOrAddTrait(Waypoints.class).getEditor(player, args);
        if (editor == null)
            return;

        if (player.isConversing() && args.argsLength() > 1) {
            player.acceptConversationInput(args.getJoinedStrings(1));
            return;
        }
        Editor.enterOrLeave(player, editor);
    }

    @Command(
            aliases = { "npc" },
            usage = "text",
            desc = "",
            modifiers = { "text" },
            min = 1,
            permission = "citizens.npc.edit.text")
    public void text(CommandContext args, Player player, NPC npc) {
        if (player.isConversing() && Editor.hasEditor(player) && args.argsLength() > 1) {
            player.acceptConversationInput(args.getJoinedStrings(1));
            return;
        }
        Editor.enterOrLeave(player, npc.getOrAddTrait(Text.class).getEditor(player));
    }
}
