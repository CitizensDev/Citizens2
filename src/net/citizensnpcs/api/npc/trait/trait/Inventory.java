package net.citizensnpcs.api.npc.trait.trait;

import java.util.Iterator;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;

@SaveId("inventory")
public class Inventory implements Trait {
    private ItemStack[] contents;

    /**
     * Gets the contents of an NPC's inventory
     * 
     * @return ItemStack array containing the inventory's contents
     */
    public ItemStack[] getContents() {
        return contents;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    @Override
    public void save(DataKey key) {
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[40];
        int index = 0;
        Iterator<DataKey> itr = key.getSubKeys().iterator();
        while (itr.hasNext()) {
            contents[index++] = getStackFromString(itr.next().name());
        }
        // Armor
        contents[36] = getStackFromString(key.getString("armor.helmet"));
        contents[37] = getStackFromString(key.getString("armor.chestplate"));
        contents[38] = getStackFromString(key.getString("armor.leggings"));
        contents[39] = getStackFromString(key.getString("armor.boots"));
        return contents;
    }

    private ItemStack getStackFromString(String item) throws NPCLoadException {
        String[] split = item.split(" ");
        int id = 1;
        short durability = 0;
        int enchantmentID = -1;
        try {
            if (split[1].contains(":")) {
                String[] data = split[1].split(":");
                id = Integer.parseInt(data[0]);
                durability = Short.parseShort(data[1]);
                if (data.length == 3)
                    enchantmentID = Integer.parseInt(data[2]);
            } else
                id = Integer.parseInt(split[1]);
            ItemStack stack = new ItemStack(id, Integer.parseInt(split[0]), durability);
            if (enchantmentID != -1) {
                Enchantment enchantment = Enchantment.getById(enchantmentID);
                if (enchantment == null)
                    throw new NPCLoadException("Invalid enchantment ID '" + enchantmentID + "'.");
                stack.addEnchantment(enchantment, enchantment.getStartLevel());
            }
            return stack;
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid item string. " + ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + contents + "}";
    }
}