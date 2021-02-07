package net.citizensnpcs.api.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;

/**
 * Represents a single inventory slot in a {@link InventoryMenu}.
 */
public class InventoryMenuSlot {
    private Set<InventoryAction> actionFilter = EnumSet.allOf(InventoryAction.class);
    private final List<Consumer<CitizensInventoryClickEvent>> handlers = Lists.newArrayList();
    private final int index;
    private final Inventory inventory;

    InventoryMenuSlot(MenuContext menu, int i) {
        this.inventory = menu.getInventory();
        this.index = i;
    }

    /**
     * Adds a click handler to this slot.
     *
     * @param func
     *            The click handler to run
     */
    public void addClickHandler(Consumer<CitizensInventoryClickEvent> func) {
        handlers.add(func);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InventoryMenuSlot other = (InventoryMenuSlot) obj;
        if (index != other.index) {
            return false;
        }
        if (inventory == null) {
            if (other.inventory != null) {
                return false;
            }
        } else if (!inventory.equals(other.inventory)) {
            return false;
        }
        return true;
    }

    public ItemStack getCurrentItem() {
        return inventory.getItem(index);
    }

    /**
     * @return The set of {@link InventoryAction}s that will be allowed
     */
    public Collection<InventoryAction> getFilter() {
        return actionFilter;
    }

    @Override
    public int hashCode() {
        int result = 31 + index;
        return 31 * result + ((inventory == null) ? 0 : inventory.hashCode());
    }

    void initialise(MenuSlot data) {
        ItemStack defaultItem = null;
        if (data.compatMaterial().length == 2) {
            Material mat = Material.getMaterial(data.compatMaterial()[0]);
            if (mat == null) {
                mat = Material.getMaterial(data.compatMaterial()[1]);
            }
            defaultItem = new ItemStack(mat, data.amount());
        } else if (data.material() != null) {
            defaultItem = new ItemStack(data.material(), data.amount());
        }
        if (defaultItem != null) {
            ItemMeta meta = defaultItem.getItemMeta();
            if (meta != null) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                if (!data.lore().equals("EMPTY")) {
                    meta.setLore(
                            Arrays.asList(Colorizer.parseColors(Messaging.tryTranslate(data.lore())).split("\\n|\n")));
                }
                if (!data.title().equals("EMPTY")) {
                    meta.setDisplayName(Colorizer.parseColors(Messaging.tryTranslate(data.title())));
                }
                defaultItem.setItemMeta(meta);
            }
        }
        inventory.setItem(index, defaultItem);
        setFilter(Arrays.asList(data.filter()));
    }

    void onClick(CitizensInventoryClickEvent event) {
        if (!actionFilter.contains(event.getAction())) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
        }
        for (Consumer<CitizensInventoryClickEvent> runnable : handlers) {
            runnable.accept(event);
        }
    }

    /**
     * Sets a new {@link ClickType} filter that will only accept clicks with the given type. An empty set is equivalent
     * to allowing all click types.
     *
     * @param filter
     *            The new filter
     */
    public void setFilter(Collection<InventoryAction> filter) {
        this.actionFilter = filter == null || filter.isEmpty() ? EnumSet.allOf(InventoryAction.class)
                : EnumSet.copyOf(filter);
    }

    /**
     * Manually set the {@link ItemStack} for this slot
     *
     * @param stack
     */
    public void setItemStack(ItemStack stack) {
        inventory.setItem(index, stack);
    }

    public void setItemStack(ItemStack item, String description) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList(Colorizer.parseColors(description).split("\n")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        inventory.setItem(index, item);
    }
}
