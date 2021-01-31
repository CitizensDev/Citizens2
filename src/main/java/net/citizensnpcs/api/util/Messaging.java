package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import net.citizensnpcs.api.npc.NPC;

public class Messaging {
    private static class DebugFormatter extends Formatter {
        private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

        @Override
        public String format(LogRecord rec) {
            Throwable exception = rec.getThrown();

            String out = this.date.format(rec.getMillis());

            out += "[" + rec.getLevel().getName().toUpperCase() + "] ";
            out += rec.getMessage() + '\n';

            if (exception != null) {
                StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));

                return out + writer;
            }

            return out;
        }
    }

    public static void configure(File debugFile, boolean debug, String messageColour, String highlightColour,
            String errorColour) {
        DEBUG = debug;
        MESSAGE_COLOUR = Colorizer.parseColors(messageColour);
        HIGHLIGHT_COLOUR = Colorizer.parseColors(highlightColour);
        ERROR_COLOUR = Colorizer.parseColors(errorColour);

        if (Bukkit.getLogger() != null) {
            LOGGER = Bukkit.getLogger();
            DEBUG_LOGGER = LOGGER;
        }
        if (debugFile != null) {
            DEBUG_LOGGER = Logger.getLogger("CitizensDebug");
            try {
                FileHandler fh = new FileHandler(debugFile.getAbsolutePath(), true);
                fh.setFormatter(new DebugFormatter());
                DEBUG_LOGGER.setUseParentHandlers(false);
                DEBUG_LOGGER.addHandler(fh);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void debug(Object... msg) {
        if (isDebugging()) {
            DEBUG_LOGGER.log(Level.INFO, "[Citizens] " + SPACE.join(msg));
        }
    }

    public static boolean isDebugging() {
        return DEBUG;
    }

    private static void log(Level level, Object... msg) {
        LOGGER.log(level, "[Citizens] " + SPACE.join(msg));
    }

    public static void log(Object... msg) {
        log(Level.INFO, msg);
    }

    public static void logTr(String key, Object... msg) {
        log(Level.INFO, Translator.translate(key, msg));
    }

    private static String prettify(String message) {
        String trimmed = message.trim();
        String messageColour = MESSAGE_COLOUR;
        if (!trimmed.isEmpty()) {
            if (trimmed.charAt(0) == ChatColor.COLOR_CHAR) {
                ChatColor test = ChatColor.getByChar(trimmed.substring(1, 2));
                if (test == null) {
                    message = messageColour + message;
                } else
                    messageColour = test.toString();
            } else {
                message = messageColour + message;
            }
        }
        message = message.replace("[[", HIGHLIGHT_COLOUR);
        message = message.replace("{{", ERROR_COLOUR);
        return CHAT_NEWLINE.matcher(message).replaceAll("<br>]]").replace("]]", messageColour);
    }

    public static void send(CommandSender sender, Object... msg) {
        sendMessageTo(sender, SPACE.join(msg));
    }

    public static void sendError(CommandSender sender, Object... msg) {
        send(sender, ERROR_COLOUR + SPACE.join(msg));
    }

    public static void sendErrorTr(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, ChatColor.RED + Translator.translate(key, msg));
    }

    private static void sendMessageTo(CommandSender sender, String rawMessage) {
        if (sender instanceof Player) {
            rawMessage = Placeholders.replace(rawMessage, (Player) sender);
        }
        rawMessage = Colorizer.parseColors(rawMessage);
        for (String message : CHAT_NEWLINE_SPLITTER.split(rawMessage)) {
            sender.sendMessage(prettify(message));
        }
    }

    public static void sendTr(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, Translator.translate(key, msg));
    }

    public static void sendWithNPC(CommandSender sender, Object msg, NPC npc) {
        send(sender, Placeholders.replace(msg.toString(), sender, npc));
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
        return TRANSLATION_MATCHER.matcher(message).matches() ? tr(message) : message;
    }

    private static final Pattern CHAT_NEWLINE = Pattern.compile("<br>|\\n", Pattern.MULTILINE);
    private static final Splitter CHAT_NEWLINE_SPLITTER = Splitter.on(CHAT_NEWLINE);
    private static boolean DEBUG = false;
    private static Logger DEBUG_LOGGER;
    private static String ERROR_COLOUR = ChatColor.RED.toString();
    private static String HIGHLIGHT_COLOUR = ChatColor.YELLOW.toString();
    private static Logger LOGGER = Logger.getLogger("Citizens");
    private static String MESSAGE_COLOUR = ChatColor.GREEN.toString();
    private static final Joiner SPACE = Joiner.on(" ").useForNull("null");
    private static final Pattern TRANSLATION_MATCHER = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z0-9]+\\.[a-zA-Z0-9.]+");
}
