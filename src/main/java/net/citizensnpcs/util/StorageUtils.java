package net.citizensnpcs.util;

import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StorageUtils {

    public static Location loadLocation(DataKey root) {
        root = root.getRelative("location");
        return new Location(Bukkit.getWorld(root.getString("world")), root.getDouble("x"), root.getDouble("y"), root
                .getDouble("z"), (float) root.getDouble("yaw", 0), (float) root.getDouble("pitch", 0));
    }

    public static ItemStack loadItemStack(DataKey root) {
        root = root.getRelative("item");
        return new ItemStack(Material.matchMaterial(root.getString("id")), root.getInt("amount"), (short) (root
                .keyExists("data") ? root.getInt("data") : 0));
    }

    public static void saveLocation(DataKey key, Location location) {
        key = key.getRelative("location");
        key.setString("world", location.getWorld().getName());
        key.setDouble("x", location.getX());
        key.setDouble("y", location.getY());
        key.setDouble("z", location.getZ());
        key.setDouble("yaw", location.getYaw());
        key.setDouble("pitch", location.getPitch());
    }
}
