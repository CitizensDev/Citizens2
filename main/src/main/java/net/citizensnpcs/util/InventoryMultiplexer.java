package net.citizensnpcs.util;

import java.util.Collection;
import java.util.function.Consumer;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

public class InventoryMultiplexer {
    private final ItemStack[] inventory;
    private final Collection<Inventory> sources;

    public InventoryMultiplexer(Collection<Inventory> sources) {
        this.sources = sources;
        int size = sources.stream().mapToInt(Inventory::getSize).sum();
        this.inventory = new ItemStack[size];
        refresh();
    }

    public InventoryMultiplexer(Inventory... inventories) {
        this(ImmutableList.copyOf(inventories));
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public void refresh() {
        int i = 0;
        for (Inventory sourceInventory : sources) {
            ItemStack[] source = sourceInventory.getContents();
            System.arraycopy(source, 0, inventory, i, source.length);
            i += source.length;
        }
    }

    public void transact(Consumer<ItemStack[]> action) {
        action.accept(inventory);
        int i = 0;
        for (Inventory source : sources) {
            ItemStack[] result = new ItemStack[source.getSize()];
            System.arraycopy(inventory, i, result, 0, result.length);
            source.setContents(result);
            i += result.length;
        }
    }
}
