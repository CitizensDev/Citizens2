package net.citizensnpcs.editor;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.Listener;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.util.Messaging;

public abstract class Editor implements Listener {
    public abstract void begin();

    public abstract void end();

    private static final Map<String, Editor> editing = new HashMap<String, Editor>();

    private static void enter(String player, Editor editor) {
        editor.begin();
        CitizensAPI.getServer().registerEvents(editor);
        editing.put(player.toLowerCase(), editor);
    }

    public static void enterOrLeave(Player player, Editor editor) {
        Editor edit = editing.get(player.getName().toLowerCase());
        if (edit == null) {
            enter(player.getName(), editor);
        } else if (edit.getClass() == editor.getClass()) {
            leave(player.getName());
        } else
            Messaging.sendError(player, "You're already in an editor!");
    }

    public static boolean hasEditor(String player) {
        return editing.containsKey(player.toLowerCase());
    }

    public static void leave(String player) {
        player = player.toLowerCase();
        if (!hasEditor(player))
            return;
        Editor editor = editing.remove(player);
        CitizensAPI.getServer().unregisterAll(editor);
        editor.end();
    }

    public static void leaveAll() {
        editing.clear();
    }
}