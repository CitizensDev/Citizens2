package net.citizensnpcs.api.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

public class SpigotUtil {
    public static int getMaxNameLength(EntityType type) {
        return type == EntityType.PLAYER ? 256 : isUsing1_13API() ? 256 : 64;
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

    private static Boolean using1_13API;
}
