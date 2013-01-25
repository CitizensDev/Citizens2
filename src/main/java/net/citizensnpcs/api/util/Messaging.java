package net.citizensnpcs.api.util;

import java.util.logging.Level;
import java.util.regex.Pattern;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class Messaging {
    private static final Pattern CHAT_NEWLINE = Pattern.compile("<br>|<n>|\\n", Pattern.MULTILINE);
    private static final Splitter CHAT_NEWLINE_SPLITTER = Splitter.on(CHAT_NEWLINE);
    private static boolean DEBUG = false;
    private static String HIGHLIGHT_COLOUR = ChatColor.YELLOW.toString();
    private static String MESSAGE_COLOUR = ChatColor.GREEN.toString();
    private static final Joiner SPACE = Joiner.on(" ").useForNull("null");

    public static void configure(boolean debug, String messageColour, String highlightColour) {
        DEBUG = debug;
        MESSAGE_COLOUR = messageColour;
        HIGHLIGHT_COLOUR = highlightColour;
    }

    public static void debug(Object... msg) {
        if (DEBUG)
            log(msg);
    }

    private static void log(Level level, Object... msg) {
        Bukkit.getLogger().log(level, SPACE.join(msg));
    }

    public static void log(Object... msg) {
        prefixedLog("[Citizens] ", msg);
    }

    public static void logTr(String key, Object... msg) {
        prefixedLog("[Citizens] ", Translator.translate(key, msg));
    }

    public static void prefixedLog(String prefix, Object... msg) {
        log(Level.INFO, prefix, msg);
    }

    private static String prettify(String message) {
        String trimmed = message.trim();
        String messageColour = Colorizer.parseColors(MESSAGE_COLOUR);
        if (!trimmed.isEmpty()) {
            if (trimmed.charAt(0) == ChatColor.COLOR_CHAR) {
                ChatColor test = ChatColor.getByChar(trimmed.substring(1, 2));
                if (test == null) {
                    message = messageColour + message;
                } else
                    messageColour = test.toString();
            } else
                message = messageColour + message;
        }
        message = message.replace("[[", Colorizer.parseColors(HIGHLIGHT_COLOUR));
        message = message.replace("]]", messageColour);
        return message;
    }

    public static void send(CommandSender sender, Object... msg) {
        sendMessageTo(sender, SPACE.join(msg));
    }

    public static void sendError(CommandSender sender, Object... msg) {
        send(sender, ChatColor.RED.toString() + SPACE.join(msg));
    }

    public static void sendErrorTr(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, ChatColor.RED + Translator.translate(key, msg));
    }

    private static void sendMessageTo(CommandSender sender, String rawMessage) {
        rawMessage = Colorizer.parseColors(rawMessage);
        for (String message : CHAT_NEWLINE_SPLITTER.split(rawMessage)) {
            sender.sendMessage(prettify(message));
        }
    }

    public static void sendTr(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, Translator.translate(key, msg));
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

    public static void severe(Object... messages) {
        log(Level.SEVERE, messages);
    }

    public static void severeTr(String key, Object... messages) {
        log(Level.SEVERE, Translator.translate(key, messages));
    }

    public static String tr(String key, Object... messages) {
        return prettify(Translator.translate(key, messages));
    }

    public static String tryTranslate(Object possible) {
        if (possible == null)
            return "";
        String message = possible.toString();
        int count = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '.')
                count++;
        }
        return count >= 2 ? tr(message) : message;
    }
}