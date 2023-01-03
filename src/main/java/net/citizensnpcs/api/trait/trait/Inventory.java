package net.citizensnpcs.api.trait.trait;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
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
    private InventoryCloseListener listener;
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
        if (view != null && !views.isEmpty()) {
            return view.getContents();
        }
        if (npc.isSpawned()) {
            saveContents(npc.getEntity());
        }
        return contents;
    }

    public org.bukkit.inventory.Inventory getInventoryView() {
        return view;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    @Override
    public void onDespawn() {
        if (npc.getEntity() instanceof InventoryHolder) {
            contents = ((InventoryHolder) npc.getEntity()).getInventory().getContents();
        }
    }

    @Override
    public void onSpawn() {
        setContents(contents);
        int size = npc.getEntity() instanceof Player ? 36
                : npc.getEntity() instanceof InventoryHolder
                        ? ((InventoryHolder) npc.getEntity()).getInventory().getSize()
                        : contents.length;
        int rem = size % 9;
        if (rem != 0) {
            size += 9 - rem; // round up to nearest multiple of 9
        }
        if (size > 54) {
            size = 54;
        }
        if (size < 9) {
            size = 9;
        }
        String name = npc.getName().length() >= 19 ? npc.getName().substring(0, 19) + "~" : npc.getName();
        view = Bukkit.createInventory(
                npc.getEntity() instanceof InventoryHolder ? ((InventoryHolder) npc.getEntity()) : null, size,
                name + "'s Inventory");
        for (int i = 0; i < view.getSize(); i++) {
            view.setItem(i, contents[i]);
        }
    }

    public void openInventory(Player sender) {
        if (npc.isSpawned()) {
            saveContents(npc.getEntity());
        }
        if (listener != null) {
            listener = new InventoryCloseListener();
            Bukkit.getPluginManager().registerEvents(listener, CitizensAPI.getPlugin());
        }
        for (int i = 0; i < view.getSize(); i++) {
            if (i >= contents.length)
                break;
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
        if (views.isEmpty())
            return;
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
        if (npc.isSpawned()) {
            saveContents(npc.getEntity());
        }
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

    private void saveContents(Entity entity) {
        if (view != null && !views.isEmpty()) {
            contents = view.getContents();
        } else if (entity instanceof InventoryHolder) {
            contents = ((InventoryHolder) entity).getInventory().getContents();
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
        }

        if (SUPPORT_ABSTRACT_HORSE) {
            try {
                if (npc.getEntity() instanceof AbstractHorse) {
                    dest = ((AbstractHorse) npc.getEntity()).getInventory();
                }
            } catch (Throwable t) {
                SUPPORT_ABSTRACT_HORSE = false;
                if (npc.getEntity() instanceof Horse) {
                    dest = ((Horse) npc.getEntity()).getInventory();
                }
            }
        } else {
            if (npc.getEntity() instanceof Horse) {
                dest = ((Horse) npc.getEntity()).getInventory();
            }
        }

        if (dest == null)
            return;

        if (maxCopySize == -1) {
            maxCopySize = dest.getSize();
        }

        for (int i = 0; i < maxCopySize; i++) {
            if (i < contents.length) {
                dest.setItem(i, contents[i]);
            }
        }

        if (view == null)
            return;

        for (int i = 0; i < maxCopySize; i++) {
            if (i < contents.length && i < view.getSize()) {
                view.setItem(i, contents[i]);
            }
        }
    }

    public void setItem(int slot, ItemStack item) {
        if (item != null) {
            item = item.clone();
        }
        if (view != null && view.getSize() > slot) {
            view.setItem(slot, item);
        } else if (contents.length > slot) {
            contents[slot] = item;
        } else {
            throw new IndexOutOfBoundsException();
        }

        if (npc.getEntity() instanceof InventoryHolder) {
            ((InventoryHolder) npc.getEntity()).getInventory().setItem(slot, item);
        }

        if (slot == 0 && npc.getEntity() instanceof LivingEntity) {
            npc.getOrAddTrait(Equipment.class).setItemInHand(item);
        }
    }

    void setItemInHand(ItemStack item) {
        if (item != null) {
            item = item.clone();
        }
        if (view != null && view.getSize() > 0) {
            view.setItem(0, item);
        } else if (contents.length > 0) {
            contents[0] = item;
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + Arrays.toString(contents) + "}";
    }

    private class InventoryCloseListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void inventoryCloseEvent(InventoryCloseEvent event) {
            if (!views.contains(event.getView()))
                return;
            ItemStack[] contents = event.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                Inventory.this.contents[i] = contents[i];
                if (i == 0) {
                    if (npc.getEntity() instanceof LivingEntity) {
                        npc.getOrAddTrait(Equipment.class).setItemInHand(contents[i]);
                    }
                }
            }
            if (npc.getEntity() instanceof InventoryHolder) {
                try {
                    int maxSize = ((InventoryHolder) npc.getEntity()).getInventory().getStorageContents().length;
                    ((InventoryHolder) npc.getEntity()).getInventory()
                            .setStorageContents(Arrays.copyOf(contents, maxSize));
                } catch (NoSuchMethodError e) {
                    int maxSize = ((InventoryHolder) npc.getEntity()).getInventory().getContents().length;
                    ((InventoryHolder) npc.getEntity()).getInventory().setContents(Arrays.copyOf(contents, maxSize));
                }
            }
            views.remove(event.getView());
        }
    }

    private static boolean SUPPORT_ABSTRACT_HORSE = true;
}
