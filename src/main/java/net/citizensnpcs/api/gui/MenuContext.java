package net.citizensnpcs.api.gui;

import java.util.Collections;
import java.util.Map;

import org.bukkit.inventory.Inventory;

import com.google.common.collect.Maps;

/**
 * A context class passed into the constructor of a {@link Menu} instance.
 */
public class MenuContext implements SlotSource {
    private final Map<String, Object> data = Maps.newHashMap();
    private final Inventory inventory;
    private final InventoryMenu menu;
    private final InventoryMenuSlot[] slots;

    public MenuContext(InventoryMenu menu, InventoryMenuSlot[] slots, Inventory inventory) {
        this(menu, slots, inventory, Collections.emptyMap());
    }

    public MenuContext(InventoryMenu menu, InventoryMenuSlot[] slots, Inventory inventory, Map<String, Object> data) {
        this.inventory = inventory;
        this.slots = slots;
        this.menu = menu;
        this.data.putAll(data);
    }

    public Map<String, Object> data() {
        return data;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public InventoryMenu getMenu() {
        return menu;
    }

    @Override
    public InventoryMenuSlot getSlot(int i) {
        if (slots[i] == null) {
            return slots[i] = new InventoryMenuSlot(this, i);
        }
        return slots[i];
    }
}
