package net.citizensnpcs.api.gui;

import java.util.Map;

import org.bukkit.inventory.Inventory;

import com.google.common.collect.Maps;

/**
 * A context class passed into the constructor of a {@link Menu} instance.
 */
public class MenuContext {
    private final Map<String, Object> data = Maps.newHashMap();
    private final Inventory inventory;
    private final InventoryMenu parent;

    public MenuContext(InventoryMenu parent, Inventory inventory, Map<String, Object> data) {
        this.inventory = inventory;
        this.parent = parent;
        this.data.putAll(data);
    }

    public Map<String, Object> data() {
        return data;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public InventoryMenu getParent() {
        return parent;
    }
}
