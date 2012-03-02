package net.citizensnpcs.editor;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.util.Messaging;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Editor implements Listener {
    public abstract void begin();

    public abstract void end();

    private static final Map<String, Editor> editing = new HashMap<String, Editor>();

    private static void enter(Player player, Editor editor) {
        editor.begin();
        player.getServer().getPluginManager()
                .registerEvents(editor, player.getServer().getPluginManager().getPlugin("Citizens"));
        editing.put(player.getName(), editor);
    }

    public static void enterOrLeave(Player player, Editor editor) {
        Editor edit = editing.get(player.getName());
        if (edit == null)
            enter(player, editor);
        else if (edit.getClass() == editor.getClass())
            leave(player);
        else
            Messaging.sendError(player, "You're already in an editor!");
    }

    public static boolean hasEditor(Player player) {
        return editing.containsKey(player.getName());
    }

    public static void leave(Player player) {
        if (!hasEditor(player))
            return;
        Editor editor = editing.remove(player.getName());
        HandlerList.unregisterAll(editor);
        editor.end();
    }

    public static void leaveAll() {
        editing.clear();
    }
}