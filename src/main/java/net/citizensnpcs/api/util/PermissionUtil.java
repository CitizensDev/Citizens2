package net.citizensnpcs.api.util;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.milkbowl.vault.permission.Permission;

public class PermissionUtil {
    public static Boolean inGroup(Collection<String> groups, Player player) {
        if (!SUPPORT_PERMISSION)
            return null;
        try {
            Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            return groups.stream().anyMatch(group -> permission.playerInGroup(player, group));
        } catch (Throwable t) {
            SUPPORT_PERMISSION = false;
            return null;
        }
    }

    private static boolean SUPPORT_PERMISSION = true;
}
