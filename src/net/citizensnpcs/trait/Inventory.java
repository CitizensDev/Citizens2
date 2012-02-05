package net.citizensnpcs.trait;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;

@SaveId("inventory")
public class Inventory implements Trait {
    private ItemStack[] contents;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public Inventory() {
    }

    public Inventory(org.bukkit.inventory.PlayerInventory inventory) {
        contents = inventory.getContents();
        helmet = inventory.getHelmet();
        chestplate = inventory.getChestplate();
        leggings = inventory.getLeggings();
        boots = inventory.getBoots();
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getArmorContents() {
        ItemStack[] armor = new ItemStack[4];
        armor[0] = helmet;
        armor[1] = chestplate;
        armor[2] = leggings;
        armor[3] = boots;
        return armor;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
        // Armor
        helmet = getItemStack(key.getRelative("armor.helmet"));
        chestplate = getItemStack(key.getRelative("armor.chestplate"));
        leggings = getItemStack(key.getRelative("armor.leggings"));
        boots = getItemStack(key.getRelative("armor.boots"));
    }

    @Override
    public void save(DataKey key) {
        int index = 0;
        for (ItemStack item : contents)
            saveItem(item, key.getRelative(String.valueOf(index++)));

        DataKey armorKey = key.getRelative("armor");
        saveItem(helmet, armorKey.getRelative("helmet"));
        saveItem(chestplate, armorKey.getRelative("chestplate"));
        saveItem(leggings, armorKey.getRelative("leggings"));
        saveItem(boots, armorKey.getRelative("boots"));
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[36];
        for (DataKey slotKey : key.getIntegerSubKeys()) {
            contents[Integer.parseInt(slotKey.name())] = getItemStack(slotKey);
        }
        return contents;
    }

    private ItemStack getItemStack(DataKey key) throws NPCLoadException {
        try {
            ItemStack item = new ItemStack(Material.getMaterial(key.getString("name")), key.getInt("amount"),
                    (short) key.getLong("data"));
            if (key.keyExists("enchantments")) {
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                for (DataKey subKey : key.getRelative("enchantments").getSubKeys()) {
                    Enchantment enchantment = Enchantment.getByName(subKey.name().toUpperCase().replace('-', '_'));
                    if (enchantment != null)
                        enchantments.put(enchantment, subKey.getInt(""));
                }
                item.addEnchantments(enchantments);
            }
            return item;
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid item.");
        }
    }

    private void saveItem(ItemStack item, DataKey key) {
        key.setString("name", item.getType().toString());
        key.setInt("amount", item.getAmount());
        key.setLong("data", item.getDurability());

        for (Enchantment enchantment : item.getEnchantments().keySet())
            key.getRelative("enchantments").setInt(enchantment.getName().toLowerCase().replace('_', '-'),
                    item.getEnchantmentLevel(enchantment));
    }

    @Override
    public String toString() {
        return "Inventory{contents:" + contents + "; helmet:" + helmet + "; chestplate:" + chestplate + "; leggings:"
                + leggings + "; boots:" + boots + "}";
    }
}