package net.citizensnpcs.util;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.util.Colorizer;

import org.bukkit.ChatColor;

public class StringHelper {
    public static String wrap(Object string) {
        return wrap(string, Colorizer.parseColors(Setting.MESSAGE_COLOUR.asString()));
    }

    public static String wrap(Object string, ChatColor colour) {
        return wrap(string, colour.toString());
    }

    public static String wrap(Object string, String colour) {
        return Colorizer.parseColors(Setting.HIGHLIGHT_COLOUR.asString()) + string.toString() + colour;
    }

    public static String wrapHeader(Object string) {
        String highlight = Setting.HIGHLIGHT_COLOUR.asString();
        return highlight + "=====[ " + string.toString() + highlight + " ]=====";
    }
}