package net.citizensnpcs.util;

import java.security.acl.Owner;
import java.util.Arrays;
import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.abstraction.CommandSender;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.npc.NPC;

import com.google.common.base.Joiner;

public class Messaging {
    private static final Joiner SPACE = Joiner.on(" ").useForNull("null");

    public static void debug(Object... msg) {
        if (Setting.DEBUG_MODE.asBoolean())
            log(msg);
    }

    public static void log(Level level, Object... msg) {
        Bukkit.getLogger().log(level, "[Citizens] " + SPACE.join(msg));
    }

    public static void log(Object... msg) {
        log(Level.INFO, msg);
    }

    private static String getFormatted(Object[] msg) {
        String toFormat = msg[0].toString();
        Object[] args = msg.length > 1 ? Arrays.copyOfRange(msg, 1, msg.length) : new Object[] {};
        return String.format(toFormat, args);
    }

    public static void logF(Object... msg) {
        log(getFormatted(msg));
    }

    public static void send(CommandSender sender, Object... msg) {
        String joined = SPACE.join(msg);
        joined = StringHelper.parseColors(joined);
        sender.sendMessage(joined);
    }

    public static void sendF(CommandSender sender, Object... msg) {
        String joined = getFormatted(msg);
        joined = StringHelper.parseColors(joined);
        sender.sendMessage(joined);
    }

    public static void sendError(CommandSender sender, Object... msg) {
        send(sender, ChatColor.RED.toString() + SPACE.join(msg));
    }

    public static void sendErrorF(CommandSender sender, Object... msg) {
        sendF(sender, ChatColor.RED.toString() + SPACE.join(msg));
    }

    public static void sendWithNPC(CommandSender sender, Object msg, NPC npc) {
        String send = msg.toString();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            send = send.replace("<player>", player.getName());
            send = send.replace("<world>", player.getWorld().getName());
        }
        send = send.replace("<owner>", npc.getAttachment(Owner.class).getOwner());
        send = send.replace("<npc>", npc.getName());
        send = send.replace("<id>", Integer.toString(npc.getId()));

        send(sender, send);
    }

    public static void severe(Object... messages) {
        log(Level.SEVERE, messages);
    }

    public static void severeF(Object... messages) {
        log(Level.SEVERE, getFormatted(messages));
    }
}