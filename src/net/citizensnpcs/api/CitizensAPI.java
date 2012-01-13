package net.citizensnpcs.api;


/**
 * Contains methods used in order to access the Citizens API
 */
public class CitizensAPI {
    private static CitizensPlugin citizens;

    public static CitizensPlugin getInstance() {
        return citizens;
    }

    public static void setInstance(CitizensPlugin plugin) {
        if (citizens != null)
            throw new IllegalArgumentException("already set");
        citizens = plugin;
    }
}