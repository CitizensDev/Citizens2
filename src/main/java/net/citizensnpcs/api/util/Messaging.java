package net.citizensnpcs.api.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.TokenParser;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class Messaging {
    private static class DebugFormatter extends Formatter {
        private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

        @Override
        public String format(LogRecord rec) {
            Throwable exception = rec.getThrown();

            String out = this.date.format(rec.getMillis());

            out += "[" + rec.getLevel().getName().toUpperCase(Locale.ROOT) + "] ";
            out += rec.getMessage() + '\n';

            if (exception != null) {
                StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));

                return out + writer;
            }
            return out;
        }
    }

    public static void configure(File debugFile, boolean debug, boolean resetFormattingOnColorChange,
            String messageColour, String highlightColour, String errorColour) {
        RESET_FORMATTING_ON_COLOR_CHANGE = resetFormattingOnColorChange;
        DEBUG = debug;
        MESSAGE_COLOUR = messageColour.replace("<a>", "<green>");
        HIGHLIGHT_COLOUR = highlightColour.replace("<e>", "<yellow>");
        if (HIGHLIGHT_COLOUR.equals("yellow")) {
            HIGHLIGHT_COLOUR = "<yellow>";
        }
        ERROR_COLOUR = errorColour.replace("<c>", "<red>");

        if (Bukkit.getLogger() != null) {
            LOGGER = Bukkit.getLogger();
            DEBUG_LOGGER = LOGGER;
        }
        if (CitizensAPI.getPlugin() != null) {
            try {
                if (MinecraftComponentSerializer.isSupported()) {
                    AUDIENCES = BukkitAudiences.create(CitizensAPI.getPlugin());
                } else {
                    Messaging.severe(
                            "Unable to load Adventure, colors will not work. Typically this means that adventure-platform-bukkit needs to be updated which can take a few weeks.");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
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

    private static String convertLegacyCodes(String message) {
        if (message == null)
            return null;
        message = ChatColor.translateAlternateColorCodes('&', message);
        Matcher m = HEX_MATCHER.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<#$1$2$3$4$5$6>");
        }
        m.appendTail(sb);

        m = HEX_CODE_MATCHER.matcher(sb.toString());
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<$1>");
        }
        m.appendTail(sb);

        m = LEGACY_COLORCODE_MATCHER.matcher(sb.toString());
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, COLORCODE_CONVERTER.get(
                    m.group(1) == null ? m.group(2).toLowerCase(Locale.ROOT) : m.group(1).toLowerCase(Locale.ROOT)));
        }
        m.appendTail(sb);
        if (!RESET_FORMATTING_ON_COLOR_CHANGE || MINIMESSAGE_COLORCODE_MATCHER == null)
            return sb.toString();
        return MINIMESSAGE_COLORCODE_MATCHER.matcher(sb.toString()).replaceAll("$0<csr>");
    }

    public static void debug(Object... msg) {
        if (isDebugging()) {
            DEBUG_LOGGER.log(Level.INFO, "[Citizens] " + SPACE.join(msg));
        }
    }

    public static void idebug(Supplier<String> msg) {
        debug(msg.get());
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

    public static Object minecraftComponentFromRawMessage(String raw) {
        if (AUDIENCES != null && MINIMESSAGE != null)
            return MinecraftComponentSerializer.get().serialize(MINIMESSAGE.deserialize(convertLegacyCodes(raw)));

        return null;
    }

    public static String parseComponents(String raw) {
        if (AUDIENCES != null && MINIMESSAGE != null)
            return BukkitComponentSerializer.legacy().serialize(MINIMESSAGE.deserialize(convertLegacyCodes(raw)));

        return convertLegacyCodes(raw);
    }

    public static List<String> parseComponentsList(String raw) {
        return CHAT_NEWLINE_SPLITTER.splitToStream(raw).map(s -> parseComponents(s)).collect(Collectors.toList());
    }

    private static String prettify(String message) {
        String trimmed = message.trim();
        String messageColour = MESSAGE_COLOUR;
        String parsed = convertLegacyCodes(trimmed);
        if (!parsed.isEmpty()) {
            if (parsed.charAt(0) == ChatColor.COLOR_CHAR) {
                ChatColor test = ChatColor.getByChar(parsed.substring(1, 2));
                if (test == null) {
                    message = messageColour + message;
                } else {
                    messageColour = test.toString();
                }
            } else {
                message = messageColour + message;
            }
        }
        message = CHAT_NEWLINE.matcher(message).replaceAll("<reset><br>]]");
        message = HIGHLIGHT_MATCHER.matcher(message).replaceAll(HIGHLIGHT_COLOUR);
        message = ERROR_MATCHER.matcher(message).replaceAll(ERROR_COLOUR);
        return message.replace("]]", MESSAGE_COLOUR);
    }

    public static void send(CommandSender sender, Object... msg) {
        sendMessageTo(sender, SPACE.join(msg), true);
    }

    public static void sendColorless(CommandSender sender, Object... msg) {
        sendMessageTo(sender, SPACE.join(msg), false);
    }

    public static void sendError(CommandSender sender, Object... msg) {
        send(sender, ERROR_COLOUR + SPACE.join(msg));
    }

    public static void sendErrorTr(CommandSender sender, String key, Object... msg) {
        send(sender, ERROR_COLOUR + Translator.translate(key, msg));
    }

    private static void sendMessageTo(CommandSender sender, String rawMessage, boolean messageColor) {
        for (String message : CHAT_NEWLINE_SPLITTER.split(rawMessage)) {
            if (messageColor) {
                message = prettify(message);
            }
            if (AUDIENCES != null) {
                AUDIENCES.sender(sender).sendMessage(MINIMESSAGE.deserialize(convertLegacyCodes(message)));
            } else {
                sender.sendMessage(convertLegacyCodes(rawMessage));
            }
        }
    }

    public static void sendTr(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, Translator.translate(key, msg), true);
    }

    public static void sendTrColorless(CommandSender sender, String key, Object... msg) {
        sendMessageTo(sender, Translator.translate(key, msg), false);
    }

    public static void sendWithNPC(CommandSender sender, Object msg, NPC npc) {
        sendMessageTo(sender, Placeholders.replace(msg.toString(), sender, npc), true);
    }

    public static void sendWithNPCColorless(CommandSender sender, Object msg, NPC npc) {
        sendMessageTo(sender, Placeholders.replace(msg.toString(), sender, npc), false);
    }

    public static void severe(Object... messages) {
        log(Level.SEVERE, messages);
    }

    public static void severeTr(String key, Object... messages) {
        log(Level.SEVERE, Translator.translate(key, messages));
    }

    public static String stripColor(String raw) {
        String converted = convertLegacyCodes(raw);
        if (DECORATION_TAGS != null) {
            converted = stripTags(converted, DECORATION_TAGS);
        }
        return ChatColor.stripColor(converted).replace("<csr>", "");
    }

    private static String stripTags(String text, TagResolver resolver) {
        StringBuilder sb = new StringBuilder();
        final List<Token> root = TokenParser.tokenize(text, true);
        for (final Token token : root) {
            switch (token.type()) {
                case TEXT:
                    sb.append(text, token.startIndex(), token.endIndex());
                    break;
                case OPEN_TAG:
                case CLOSE_TAG:
                case OPEN_CLOSE_TAG:
                    if (token.childTokens().isEmpty()) {
                        sb.append(text, token.startIndex(), token.endIndex());
                        continue;
                    }
                    if (resolver.has(TokenParser.TagProvider
                            .sanitizePlaceholderName(token.childTokens().get(0).get(text).toString()))) {
                        continue;
                    } else {
                        sb.append(text, token.startIndex(), token.endIndex());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported token type " + token.type());
            }
        }
        return sb.toString();
    }

    public static String tr(String key, Object... messages) {
        return prettify(Translator.translate(key, messages));
    }

    public static String tryTranslate(Object possible) {
        if (possible == null)
            return "";
        String message = possible.toString();
        return TRANSLATION_MATCHER.matcher(message).find() ? tr(message) : message;
    }

    public static void warn(Object... string) {
        log(Level.WARNING, string);
    }

    private static BukkitAudiences AUDIENCES;
    private static final Pattern CHAT_NEWLINE = Pattern.compile("<br>|\\n", Pattern.MULTILINE);
    private static final Splitter CHAT_NEWLINE_SPLITTER = Splitter.on(CHAT_NEWLINE);
    private static final Map<String, String> COLORCODE_CONVERTER = Maps.newHashMap();
    private static boolean DEBUG = false;
    private static Logger DEBUG_LOGGER;
    private static TagResolver DECORATION_TAGS;
    private static String ERROR_COLOUR = "<red>";
    private static final Pattern ERROR_MATCHER = Pattern.compile("{{", Pattern.LITERAL);
    private static final Pattern HEX_CODE_MATCHER = Pattern
            .compile("&(#[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f])", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_MATCHER = Pattern.compile(
            "&x&([0-9a-f])&([0-9a-f])&([0-9a-f])&([0-9a-f])&([0-9a-f])&([0-9a-f])".replace('&', ChatColor.COLOR_CHAR),
            Pattern.CASE_INSENSITIVE);
    private static String HIGHLIGHT_COLOUR = "<yellow>";
    private static final Pattern HIGHLIGHT_MATCHER = Pattern.compile("[[", Pattern.LITERAL);
    private static final Pattern LEGACY_COLORCODE_MATCHER = Pattern
            .compile(ChatColor.COLOR_CHAR + "([0-9a-r])|<([0-9a-f])>", Pattern.CASE_INSENSITIVE);
    private static Logger LOGGER = Logger.getLogger("Citizens");
    private static String MESSAGE_COLOUR = "<green>";
    private static MiniMessage MINIMESSAGE;
    private static Pattern MINIMESSAGE_COLORCODE_MATCHER;
    private static boolean RESET_FORMATTING_ON_COLOR_CHANGE = true;
    private static final Joiner SPACE = Joiner.on(" ").useForNull("null");
    private static final Pattern TRANSLATION_MATCHER = Pattern.compile("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+\\.[a-zA-Z0-9.]+");
    static {
        COLORCODE_CONVERTER.put("0", "<black>");
        COLORCODE_CONVERTER.put("1", "<dark_blue>");
        COLORCODE_CONVERTER.put("2", "<dark_green>");
        COLORCODE_CONVERTER.put("3", "<dark_aqua>");
        COLORCODE_CONVERTER.put("4", "<dark_red>");
        COLORCODE_CONVERTER.put("5", "<dark_purple>");
        COLORCODE_CONVERTER.put("6", "<gold>");
        COLORCODE_CONVERTER.put("7", "<gray>");
        COLORCODE_CONVERTER.put("8", "<dark_gray>");
        COLORCODE_CONVERTER.put("9", "<blue>");
        COLORCODE_CONVERTER.put("a", "<green>");
        COLORCODE_CONVERTER.put("b", "<aqua>");
        COLORCODE_CONVERTER.put("c", "<red>");
        COLORCODE_CONVERTER.put("d", "<light_purple>");
        COLORCODE_CONVERTER.put("e", "<yellow>");
        COLORCODE_CONVERTER.put("f", "<white>");
        COLORCODE_CONVERTER.put("m", "<st>");
        COLORCODE_CONVERTER.put("n", "<u>");
        COLORCODE_CONVERTER.put("k", "<obf>");
        COLORCODE_CONVERTER.put("o", "<i>");
        COLORCODE_CONVERTER.put("l", "<b>");
        COLORCODE_CONVERTER.put("r", "<reset>");
        try {
            MINIMESSAGE_COLORCODE_MATCHER = Pattern
                    .compile(
                            Joiner.on('|')
                                    .join(Collections2.transform(NamedTextColor.NAMES.values(),
                                            c -> '<' + c.toString() + '>'))
                                    + "|<#[a-f\\d]{6}>",
                            Pattern.CASE_INSENSITIVE);
            TagResolver citizensResetColorResolver = TagResolver.resolver("csr",
                    Tag.styling(s -> Arrays.stream(TextDecoration.values()).forEach(td -> s.decoration(td, false))));
            MINIMESSAGE = MiniMessage.builder().editTags(t -> t.resolver(citizensResetColorResolver)).build();
            DECORATION_TAGS = TagResolver.builder()
                    .resolvers(citizensResetColorResolver, StandardTags.clickEvent(), StandardTags.color(),
                            StandardTags.rainbow(), StandardTags.reset(), StandardTags.gradient(),
                            StandardTags.transition(), StandardTags.decorations(), StandardTags.font())
                    .build();
        } catch (Throwable t) {
        }
    }
}
