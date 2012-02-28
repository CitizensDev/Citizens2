package net.citizensnpcs.api.util;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemBuilder {

    public static ItemStack getItemStack(DataKey key) throws NPCLoadException {
        try {
            ItemStack item = new ItemStack(Material.getMaterial(key.getString("name").toUpperCase().replace('-', '_')),
                    key.getInt("amount"), (short) key.getLong("data"));
            if (key.keyExists("enchantments")) {
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                for (DataKey subKey : key.getRelative("enchantments").getSubKeys()) {
                    Enchantment enchantment = Enchantment.getByName(subKey.name().toUpperCase().replace('-', '_'));
                    if (enchantment != null && enchantment.canEnchantItem(item))
                        enchantments.put(enchantment, subKey.getInt("") <= enchantment.getMaxLevel() ? subKey
                                .getInt("") : enchantment.getMaxLevel());
                }
                item.addEnchantments(enchantments);
            }
            return item;
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid item. " + ex.getMessage());
        }
    }

    public static void saveItem(ItemStack item, DataKey key) {
        key.setString("name", item.getType().toString());
        key.setInt("amount", item.getAmount());
        key.setLong("data", item.getDurability());

        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            key.getRelative("enchantments").setInt(enchantment.getName().toLowerCase().replace('_', '-'),
                    item.getEnchantmentLevel(enchantment));
        }
    }
}