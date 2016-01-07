package net.citizensnpcs.api.trait.trait;

import java.util.Arrays;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

/**
 * Represents an NPC's inventory.
 */
@TraitName("inventory")
public class Inventory extends Trait {
    private ItemStack[] contents;

    public Inventory() {
        super("inventory");
        contents = new ItemStack[72];
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

    @Override
    public void onSpawn() {
        setContents(contents);
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[72];
        for (DataKey slotKey : key.getIntegerSubKeys())
            contents[Integer.parseInt(slotKey.name())] = ItemStorage.loadItemStack(slotKey);
        return contents;
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Player) {
            contents = ((Player) npc.getEntity()).getInventory().getContents();
        }
    }

    @Override
    public void save(DataKey key) {
        int slot = 0;
        for (ItemStack item : contents) {
            // Clear previous items to avoid conflicts
            key.removeKey(String.valueOf(slot));
            if (item != null) {
                ItemStorage.saveItem(key.getRelative(String.valueOf(slot)), item);
            }
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
        this.contents = Arrays.copyOf(contents, 72);
        if (npc.getEntity() instanceof Player) {
            ((Player) npc.getEntity()).getInventory().setContents(Arrays.copyOf(this.contents, 36));
        } else if (npc.getEntity() instanceof StorageMinecart) {
            ((StorageMinecart) npc.getEntity()).getInventory().setContents(this.contents);
        } else if (npc.getEntity() instanceof Horse) {
            ((Horse) npc.getEntity()).getInventory()
                    .setContents(Arrays.copyOf(this.contents, ((Horse) npc.getEntity()).getInventory().getSize()));
            ((Horse) npc.getEntity()).getInventory().setSaddle(this.contents[0]);
            ((Horse) npc.getEntity()).getInventory().setArmor(this.contents[1]);
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + Arrays.toString(contents) + "}";
    }
}
