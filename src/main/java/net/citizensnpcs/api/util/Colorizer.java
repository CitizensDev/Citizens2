package net.citizensnpcs.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class Colorizer {
    public static String parseColors(String parsed) {
        Matcher matcher = HEX_MATCHER.matcher(parsed);
        parsed = matcher.replaceAll("&x&$1&$2&$3&$4&$5&$6");
        matcher = COLOR_MATCHER.matcher(ChatColor.translateAlternateColorCodes('&', parsed));
        return matcher.replaceAll(GROUP);
    }

    public static String stripColors(String parsed) {
        Matcher matcher = COLOR_MATCHER.matcher(parsed);
        return matcher.replaceAll("");
    }

    private static Pattern COLOR_MATCHER;
    private static String GROUP = ChatColor.COLOR_CHAR + "$1";
    private static Pattern HEX_MATCHER = Pattern
            .compile("&#([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])", Pattern.CASE_INSENSITIVE);

    static {
        String colors = "";
        for (ChatColor color : ChatColor.values()) {
            colors += color.getChar();
        }
        COLOR_MATCHER = Pattern.compile("\\<([COLORS])\\>".replace("COLORS", colors), Pattern.CASE_INSENSITIVE);
    }
}
