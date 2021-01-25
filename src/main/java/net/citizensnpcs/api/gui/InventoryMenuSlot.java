package net.citizensnpcs.api.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryMenuSlot {
    private Set<ClickType> clickFilter = EnumSet.allOf(ClickType.class);
    private final int index;
    private final Inventory inventory;

    public InventoryMenuSlot(MenuContext menu, int i) {
        this.inventory = menu.getInventory();
        this.index = i;
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

    @Override
    public int hashCode() {
        int result = 31 + index;
        return 31 * result + ((inventory == null) ? 0 : inventory.hashCode());
    }

    public void initialise(MenuSlot data) {
        ItemStack defaultItem = null;
        if (data.material() != null) {
            defaultItem = new ItemStack(data.material(), data.amount());
        }
        inventory.setItem(index, defaultItem);
        setClickFilter(Arrays.asList(data.filter()));
    }

    public void onClick(InventoryClickEvent event) {
        if (!clickFilter.contains(event.getClick())) {
            event.setCancelled(true);
        }
    }

    public void setClickFilter(Collection<ClickType> filter) {
        this.clickFilter = filter == null || filter.isEmpty() ? EnumSet.allOf(ClickType.class) : EnumSet.copyOf(filter);
    }
}
