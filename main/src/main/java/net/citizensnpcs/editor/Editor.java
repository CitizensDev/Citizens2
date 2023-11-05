package net.citizensnpcs.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

// TODO: convert to non-static?
public abstract class Editor implements Listener {
    public abstract void begin();

    public abstract void end();

    private static void enter(Player player, Editor editor) {
        editor.begin();
        player.getServer().getPluginManager().registerEvents(editor, CitizensAPI.getPlugin());
        EDITING.put(player.getUniqueId(), editor);
    }

    public static void enterOrLeave(Player player, Editor editor) {
        if (editor == null)
            return;
        Editor edit = EDITING.get(player.getUniqueId());
        if (edit == null) {
            enter(player, editor);
        } else if (edit.getClass() == editor.getClass()) {
            leave(player);
        } else {
            Messaging.sendErrorTr(player, Messages.ALREADY_IN_EDITOR);
        }
    }

    public static boolean hasEditor(Player player) {
        return EDITING.containsKey(player.getUniqueId());
    }

    public static void leave(Player player) {
        if (!hasEditor(player))
            return;
        Editor editor = EDITING.remove(player.getUniqueId());
        HandlerList.unregisterAll(editor);
        editor.end();
    }

    public static void leaveAll() {
        for (Entry<UUID, Editor> entry : EDITING.entrySet()) {
            entry.getValue().end();
            HandlerList.unregisterAll(entry.getValue());
        }
        EDITING.clear();
    }

    private static final Map<UUID, Editor> EDITING = new HashMap<>();
}