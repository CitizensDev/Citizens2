package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStorage {
    public static ItemStack loadItemStack(DataKey root) {
        Material matched = Material.matchMaterial(root.getString("type", root.getString("id")));
        if (matched == null)
            return null;
        ItemStack res = new ItemStack(matched, root.getInt("amount"), (short) (root.getInt("durability",
                root.getInt("data", 0))));
        if (root.keyExists("mdata") && res.getData() != null) {
            res.getData().setData((byte) root.getInt("mdata"));
        }
        if (root.keyExists("enchantments")) {
            Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
            for (DataKey subKey : root.getRelative("enchantments").getSubKeys()) {
                Enchantment enchantment = Enchantment.getById(Integer.parseInt(subKey.name()));
                if (enchantment != null && enchantment.canEnchantItem(res)) {
                    int level = Math.min(subKey.getInt(""), enchantment.getMaxLevel());
                    enchantments.put(enchantment, level);
                }
            }
            res.addEnchantments(enchantments);
        }
        return res;
    }

    public static void saveItem(DataKey key, ItemStack item) {
        migrateForSave(key);
        key.setString("type", item.getType().name());
        key.setInt("amount", item.getAmount());
        key.setInt("durability", item.getDurability());
        if (item.getData() != null) {
            key.setInt("mdata", item.getData().getData());
        }

        key = key.getRelative("enchantments");
        for (Enchantment enchantment : item.getEnchantments().keySet())
            key.setInt(Integer.toString(enchantment.getId()), item.getEnchantmentLevel(enchantment));
    }

    private static void migrateForSave(DataKey key) {
        key.removeKey("data");
        key.removeKey("id");
    }
}