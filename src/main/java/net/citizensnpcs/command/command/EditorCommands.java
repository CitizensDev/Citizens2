package net.citizensnpcs.command.command;

import java.util.HashMap;
import java.util.Map;

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
import net.citizensnpcs.util.Messaging;

@Requirements(selected = true, ownership = true)
public class EditorCommands {
    private final Citizens plugin;
    private static final Map<String, Editor> editors = new HashMap<String, Editor>();

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
        toggleEditor(player, npc, "equip");
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
        toggleEditor(player, npc, "path");
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
        toggleEditor(player, npc, "text");
    }

    public static void removeEditor(Player player) {
        if (editors.containsKey(player.getName()))
            editors.remove(player.getName());
    }

    private void toggleEditor(Player player, NPC npc, String name) {
        if (editors.containsKey(player.getName())) {
            if (editors.get(player.getName()).getName().equals(name)) {
                editors.get(player.getName()).end();
                editors.remove(player.getName());
            } else {
                Messaging.sendError(player, "You can only be in one editor at a time.");
                Messaging.sendError(player, "Type /npc " + name + " to exit the current editor.");
            }
        } else {
            Editor editor = getEditor(player, npc, name);
            editors.put(player.getName(), editor);
            editor.begin();
        }
    }

    private Editor getEditor(Player player, NPC npc, String name) {
        if (name.equals("equip"))
            return new EquipmentEditor(plugin, player, npc);
        else if (name.equals("path"))
            return npc.getTrait(Waypoints.class).getEditor(player);
        else if (name.equals("text"))
            return new TextEditor();

        return null;
    }
}