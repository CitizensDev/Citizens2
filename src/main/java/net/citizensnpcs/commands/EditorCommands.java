package net.citizensnpcs.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.editor.EquipmentEditor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Requirements(selected = true, ownership = true)
public class EditorCommands {

    @Command(
            aliases = { "시민" },
            usage = "장비",
            desc = "장비 수정자로 전환합니다",
            modifiers = { "장비" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.equip")
    @Requirements(selected = true, ownership = true)
    public void equip(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, new EquipmentEditor(player, npc));
    }

    @Command(
            aliases = { "시민" },
            usage = "경로",
            desc = "웨이포인트 수정자로 전환합니다",
            modifiers = { "경로" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.path")
    @Requirements(selected = true, ownership = true, excludedTypes = { EntityType.BLAZE, EntityType.ENDER_DRAGON,
            EntityType.GHAST, EntityType.BAT, EntityType.WITHER, EntityType.SQUID })
    public void path(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Waypoints.class).getEditor(player));
    }

    @Command(
            aliases = { "시민" },
            usage = "대화",
            desc = "대화 수정자로 전환합니다",
            modifiers = { "대화" },
            min = 1,
            max = 1,
            permission = "citizens.npc.edit.text")
    public void text(CommandContext args, Player player, NPC npc) {
        Editor.enterOrLeave(player, npc.getTrait(Text.class).getEditor(player));
    }
}