package net.citizensnpcs.util;

import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class Messaging {
    private static final Joiner SPACE = Joiner.on(" ");

    public static void debug(Object... msg) {
        if (Setting.DEBUG_MODE.asBoolean())
            log(msg);
    }

    public static void log(Level level, Object... msg) {
        Bukkit.getLogger().log(level, "[Citizens] " + SPACE.join(msg));
    }

    public static void log(Object... msg) {
        log(Level.INFO, SPACE.join(msg));
    }

    public static void send(CommandSender sender, Object msg) {
        sender.sendMessage(StringHelper.parseColors(msg.toString()));
    }

    public static void sendError(CommandSender sender, Object msg) {
        send(sender, ChatColor.RED.toString() + msg);
    }

    public static void sendWithNPC(CommandSender sender, Object msg, NPC npc) {
        String send = msg.toString();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            send = send.replace("<player>", player.getName());
            send = send.replace("<world>", player.getWorld().getName());
        }
        send = send.replace("<owner>", npc.getTrait(Owner.class).getOwner());
        send = send.replace("<npc>", npc.getName());
        send = send.replace("<id>", Integer.toString(npc.getId()));

        send(sender, send);
    }
}