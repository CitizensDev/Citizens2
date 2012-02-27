package net.citizensnpcs.editor;

import java.util.Map;

import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.google.common.collect.Maps;

public abstract class Editor implements Listener {
    public abstract void begin();

    public abstract void end();

    public static void enterEditor(Player player, Editor editor) {
        if (editing.containsKey(player.getName())) {
            Messaging.sendError(player, "You're already in an editor!");
            return;
        }
        editor.begin();
        Bukkit.getPluginManager().registerEvents(editor, Bukkit.getPluginManager().getPlugin("Citizens"));
        editing.put(player.getName(), editor);
    }

    public static void leaveEditor(Player player) {
        if (!editing.containsKey(player.getName()))
            return;
        Editor editor = editing.remove(player.getName());
        HandlerList.unregisterAll(editor);
        editor.end();
    }

    private static final Map<String, Editor> editing = Maps.newHashMap();
}