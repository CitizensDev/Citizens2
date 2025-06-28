package net.citizensnpcs.api.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.util.Messaging;

/**
 * Represents a single inventory slot in a {@link InventoryMenu}.
 */
public class InventoryMenuSlot {
    private Set<InventoryAction> actionFilter;
    private final List<Consumer<CitizensInventoryClickEvent>> handlers = Lists.newArrayList();
    private final int index;
    private final Inventory inventory;

    InventoryMenuSlot(MenuContext menu, int index) {
        this.inventory = menu.getInventory();
        this.index = index;
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

    public void clear() {
        handlers.clear();
        actionFilter = null;
        setItemStack(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        InventoryMenuSlot other = (InventoryMenuSlot) obj;
        if ((index != other.index) || !Objects.equals(inventory, other.inventory))
            return false;
        return true;
    }

    public List<Consumer<CitizensInventoryClickEvent>> getClickHandlers() {
        return handlers;
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
        return 31 * result + (inventory == null ? 0 : inventory.hashCode());
    }

    void initialise(MenuSlot data) {
        ItemStack defaultItem = null;
        if (data.compatMaterial().length > 1) {
            Material mat = null;
            for (String str : data.compatMaterial()) {
                mat = Material.getMaterial(str);
                if (mat != null) {
                    break;
                }
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
                    meta.setLore(Arrays.asList(
                            Messaging.parseComponents(Messaging.tryTranslate(data.lore())).split("\\n|\n|<br>")));
                }
                if (!data.title().equals("EMPTY")) {
                    meta.setDisplayName(Messaging.parseComponents(Messaging.tryTranslate(data.title())));
                }
                defaultItem.setItemMeta(meta);
            }
        }
        inventory.setItem(index, defaultItem);
    }

    void onClick(CitizensInventoryClickEvent event) {
        if (actionFilter == null && handlers.isEmpty()
                || actionFilter != null && !actionFilter.contains(event.getAction())) {
            event.setCancelled(true);
        }
        for (Consumer<CitizensInventoryClickEvent> runnable : Lists.newArrayList(handlers)) {
            runnable.accept(event);
        }
    }

    public void setClickHandler(Consumer<CitizensInventoryClickEvent> handler) {
        handlers.clear();
        handlers.add(handler);
    }

    public void setDescription(String description) {
        ItemStack item = inventory.getItem(index);
        ItemMeta meta = item.getItemMeta();
        List<String> list = Messaging.parseComponentsList(description);
        meta.setDisplayName(ChatColor.RESET + list.get(0));
        meta.setLore(list.subList(1, list.size()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        inventory.setItem(index, item);
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

    public void setItemStack(ItemStack stack, String name) {
        setItemStack(stack, name, null);
    }

    public void setItemStack(ItemStack stack, String name, String description) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + Messaging.parseComponents(name));
        if (description != null) {
            meta.setLore(Arrays.asList(NEWLINE_MATCHER.split(Messaging.parseComponents(description))));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }

    private static Pattern NEWLINE_MATCHER = Pattern.compile("\n|\\n|<br>");
}
