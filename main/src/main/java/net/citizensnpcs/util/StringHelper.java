package net.citizensnpcs.util;

public class StringHelper {
    public static String wrap(Object string) {
        return "[[" + string.toString() + "]]";
    }

    public static String wrap(Object string, String colour) {
        return "[[" + string.toString() + colour;
    }

    public static String wrapHeader(Object string) {
        return "[[=====[ ]]" + string.toString() + "[[ ]=====";
    }
}