package net.citizensnpcs.util;

public class StringHelper {
    public static String wrap(Object string) {
        return "<yellow>" + string.toString() + "</yellow>";
    }

    public static String wrap(Object string, String colour) {
        return "[[" + string.toString() + colour;
    }

    public static String wrapHeader(Object string) {
        return "<yellow>=====[</yellow> " + string.toString() + "<yellow> ]=====";
    }
}