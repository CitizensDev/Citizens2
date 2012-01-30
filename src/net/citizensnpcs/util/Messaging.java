package net.citizensnpcs.util;

import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messaging {

    public static void debug(Object msg) {
        if (Setting.DEBUG_MODE.asBoolean())
            log(msg);
    }

    public static void log(Level level, Object msg) {
        Bukkit.getLogger().log(level, "[Citizens] " + msg);
    }

    public static void log(Object msg) {
        log(Level.INFO, msg);
    }

    public static void send(Player player, Object msg) {
        String send = msg.toString();

        for (ChatColor color : ChatColor.values())
            send = send.replace("<" + color.getChar() + ">", color.toString());

        player.sendMessage(send);
    }

    public static void sendWithNPC(Player player, Object msg, NPC npc) {
        String send = msg.toString();

        send = send.replace("<npc>", npc.getName());
        send = send.replace("<id>", Integer.toString(npc.getId()));

        send(player, send);
    }

    public static void sendError(Player player, Object msg) {
        send(player, ChatColor.RED.toString() + msg);
    }
}