package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStorage {

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

    public static void saveItem(DataKey key, ItemStack item) {
        key.setString("item", item.getType().name());
        key.setInt("amount", item.getAmount());
        key.setInt("data", item.getDurability());

        key = key.getRelative("enchantments");
        for (Enchantment enchantment : item.getEnchantments().keySet())
            key.setInt(Integer.toString(enchantment.getId()), item.getEnchantmentLevel(enchantment));
    }
}