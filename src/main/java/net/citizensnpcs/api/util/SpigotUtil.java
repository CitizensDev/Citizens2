package net.citizensnpcs.api.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

public class SpigotUtil {
    public static boolean checkYSafe(double y, World world) {
        if (!SUPPORT_WORLD_HEIGHT || world == null) {
            return y >= 0 && y <= 255;
        }
        try {
            return y >= world.getMinHeight() && y <= world.getMaxHeight();
        } catch (Throwable t) {
            SUPPORT_WORLD_HEIGHT = false;
            return y >= 0 && y <= 255;
        }
    }

    public static int getMaxNameLength(EntityType type) {
        return isUsing1_13API() ? 256 : 64;
    }

    private static int[] getVersion() {
        if (BUKKIT_VERSION == null) {
            String version = Bukkit.getVersion();
            if (version == null || version.isEmpty()) {
                return new int[] { 1, 8 };
            }
            String[] parts = version.split("_");
            return BUKKIT_VERSION = new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
        }
        return BUKKIT_VERSION;
    }

    public static boolean isUsing1_13API() {
        if (using1_13API == null) {
            try {
                Enchantment.getByKey(Enchantment.ARROW_DAMAGE.getKey());
                using1_13API = true;
            } catch (Exception ex) {
                using1_13API = false;
            } catch (NoSuchMethodError ex) {
                using1_13API = false;
            }
        }
        return using1_13API;
    }

    private static int[] BUKKIT_VERSION = null;
    private static boolean SUPPORT_WORLD_HEIGHT = true;
    private static Boolean using1_13API;
}
