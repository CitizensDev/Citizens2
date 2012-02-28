package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class StorageUtils {

    public static Location loadLocation(DataKey root) {
        root = root.getRelative("location");
        return new Location(Bukkit.getWorld(root.getString("world")), root.getDouble("x"), root.getDouble("y"), root
                .getDouble("z"), (float) root.getDouble("yaw", 0), (float) root.getDouble("pitch", 0));
    }

    public static ItemStack loadItemStack(DataKey root) {
        ItemStack res = new ItemStack(Material.matchMaterial(root.getString("id")), root.getInt("amount"),
                (short) (root.keyExists("data") ? root.getInt("data") : 0));
        if (root.keyExists("enchantments")) {
            Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
            for (DataKey subKey : root.getRelative("enchantments").getSubKeys()) {
                Enchantment enchantment = Enchantment.getById(Integer.parseInt(subKey.name()));
                if (enchantment != null && enchantment.canEnchantItem(res))
                    enchantments.put(enchantment, subKey.getInt("") <= enchantment.getMaxLevel() ? subKey.getInt("")
                            : enchantment.getMaxLevel());
            }
            res.addEnchantments(enchantments);
        }
        return res;
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

    public static void saveItem(DataKey key, ItemStack item) {
        key.setInt("id", item.getTypeId());
        key.setInt("amount", item.getAmount());
        key.setInt("data", item.getDurability());

        key = key.getRelative("enchantments");
        for (Enchantment enchantment : item.getEnchantments().keySet())
            key.setInt(Integer.toString(enchantment.getId()), item.getEnchantmentLevel(enchantment));
    }
}