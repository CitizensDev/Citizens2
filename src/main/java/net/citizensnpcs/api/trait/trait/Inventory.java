package net.citizensnpcs.api.trait.trait;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
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
    private org.bukkit.inventory.Inventory view;
    private final Set<InventoryView> views = new HashSet<InventoryView>();

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

    @EventHandler(ignoreCancelled = true)
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if (!views.contains(event.getView()))
            return;
        ItemStack[] contents = event.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            this.contents[i] = contents[i];
        }
        if (npc.getEntity() instanceof InventoryHolder) {
            ((InventoryHolder) npc.getEntity()).getInventory().setStorageContents(contents);
        }
        views.remove(event.getView());
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    @Override
    public void onSpawn() {
        setContents(contents);
        int size = npc.getEntity() instanceof Player ? 36
                : npc.getEntity() instanceof InventoryHolder
                        ? ((InventoryHolder) npc.getEntity()).getInventory().getSize() : contents.length;
        int rem = size % 9;
        if (rem != 0) {
            size += 9 - rem; // round up to nearest multiple of 9
        }
        view = Bukkit.createInventory(
                npc.getEntity() instanceof InventoryHolder ? ((InventoryHolder) npc.getEntity()) : null, size,
                npc.getName() + "'s Inventory");
    }

    public void openInventory(Player sender) {
        for (int i = 0; i < view.getSize(); i++) {
            view.setItem(i, contents[i]);
        }
        views.add(sender.openInventory(view));
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[72];
        for (DataKey slotKey : key.getIntegerSubKeys()) {
            contents[Integer.parseInt(slotKey.name())] = ItemStorage.loadItemStack(slotKey);
        }
        return contents;
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Player) {
            contents = ((Player) npc.getEntity()).getInventory().getContents();
        }
        Iterator<InventoryView> itr = views.iterator();
        while (itr.hasNext()) {
            InventoryView iview = itr.next();
            if (!iview.getPlayer().isValid()) {
                iview.close();
                itr.remove();
            }
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
        org.bukkit.inventory.Inventory dest = null;
        int maxCopySize = -1;
        if (npc.getEntity() instanceof Player) {
            dest = ((Player) npc.getEntity()).getInventory();
            maxCopySize = 36;
        } else if (npc.getEntity() instanceof StorageMinecart) {
            dest = ((StorageMinecart) npc.getEntity()).getInventory();
        } else if (npc.getEntity() instanceof Horse) {
            dest = ((Horse) npc.getEntity()).getInventory();
        }

        if (dest == null)
            return;
        if (maxCopySize == -1) {
            maxCopySize = dest.getSize();
        }

        for (int i = 0; i < maxCopySize; i++) {
            dest.setItem(i, contents[i]);
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + Arrays.toString(contents) + "}";
    }
}
