package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Represents an NPC's inventory.
 */
public class Inventory extends Trait {
    private ItemStack[] contents;

    public Inventory() {
        super("inventory");
        contents = new ItemStack[36];
    }

    /**
     * Gets the contents of an NPC's inventory.
     * 
     * @return ItemStack array of an NPC's inventory contents
     */
    public ItemStack[] getContents() {
        return contents;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[36];
        for (DataKey slotKey : key.getIntegerSubKeys())
            contents[Integer.parseInt(slotKey.name())] = ItemStorage.loadItemStack(slotKey);
        return contents;
    }

    @Override
    public void save(DataKey key) {
        int slot = 0;
        for (ItemStack item : contents) {
            // Clear previous items to avoid conflicts
            key.removeKey(String.valueOf(slot));
            if (item != null)
                ItemStorage.saveItem(key.getRelative(String.valueOf(slot)), item);
            slot++;
        }
    }

    /**
     * Sets the contents of an NPC's inventory.
     * 
     * @param contents
     *            ItemStack array to set as the contents of an NPC's inventory
     */
    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "Inventory{" + Arrays.toString(contents) + "}";
    }
}