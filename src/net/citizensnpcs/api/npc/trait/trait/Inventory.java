package net.citizensnpcs.api.npc.trait.trait;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@SaveId("inventory")
public class Inventory extends Trait {
    private org.bukkit.inventory.Inventory inventory;

    public Inventory() {
    }

    public Inventory(org.bukkit.inventory.Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Gets the contents of an NPC's inventory
     * 
     * @return ItemStack array of an NPC's inventory contents
     */
    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    /**
     * Sets the contents of an NPC's inventory
     * 
     * @param contents
     *            ItemStack array to set as the contents of an NPC's inventory
     */
    public void setContents(ItemStack[] contents) {
        inventory.setContents(contents);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        inventory.setContents(parseContents(key));
    }

    @Override
    public void save(DataKey key) {
        int slot = 0;
        for (ItemStack item : inventory.getContents()) {
            // Clear previous items to avoid conflicts
            key.removeKey(String.valueOf(slot));
            if (item != null)
                saveItem(item, key.getRelative(String.valueOf(slot)));
            slot++;
        }
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[inventory.getSize()];
        for (DataKey slotKey : key.getIntegerSubKeys()) {
            int index = Integer.parseInt(slotKey.name());
            if (index >= contents.length)
                continue;
            contents[index] = getItemStack(slotKey);
        }
        return contents;
    }

    private ItemStack getItemStack(DataKey key) throws NPCLoadException {
        try {
            ItemStack item = new ItemStack(Material.matchMaterial(key.getString("name")), key.getInt("amount"),
                    (short) key.getLong("data"));
            if (key.keyExists("enchantments")) {
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                for (DataKey subKey : key.getRelative("enchantments").getSubKeys()) {
                    Enchantment enchantment = Enchantment.getByName(subKey.name());
                    if (enchantment != null && enchantment.canEnchantItem(item)) {
                        enchantments.put(
                                enchantment,
                                subKey.getInt("") <= enchantment.getMaxLevel() ? subKey.getInt("") : enchantment
                                        .getMaxLevel());
                    }
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
            key.getRelative("enchantments").setInt(enchantment.getName(), item.getEnchantmentLevel(enchantment));
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + inventory.getContents() + "}";
    }
}