package net.citizensnpcs.api.trait.trait;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an NPC's inventory
 */
@SaveId("inventory")
public class Inventory extends Trait {
    private ItemStack[] contents;

    public Inventory() {
        contents = new ItemStack[36];
    }

    public Inventory(org.bukkit.inventory.Inventory inventory) {
        contents = inventory.getContents();
    }

    /**
     * Gets the contents of an NPC's inventory
     * 
     * @return ItemStack array of an NPC's inventory contents
     */
    public ItemStack[] getContents() {
        return contents;
    }

    /**
     * Sets the contents of an NPC's inventory
     * 
     * @param contents
     *            ItemStack array to set as the contents of an NPC's inventory
     */
    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    @Override
    public void save(DataKey key) {
        int slot = 0;
        for (ItemStack item : contents) {
            // Clear previous items to avoid conflicts
            key.removeKey(String.valueOf(slot));
            if (item != null)
                saveItem(item, key.getRelative(String.valueOf(slot)));
            slot++;
        }
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[36];
        for (DataKey slotKey : key.getIntegerSubKeys())
            contents[Integer.parseInt(slotKey.name())] = getItemStack(slotKey);
        return contents;
    }

    private ItemStack getItemStack(DataKey key) throws NPCLoadException {
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

    private void saveItem(ItemStack item, DataKey key) {
        key.setString("name", item.getType().toString());
        key.setInt("amount", item.getAmount());
        key.setLong("data", item.getDurability());

        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            key.getRelative("enchantments").setInt(enchantment.getName().toLowerCase().replace('_', '-'),
                    item.getEnchantmentLevel(enchantment));
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + contents + "}";
    }
}