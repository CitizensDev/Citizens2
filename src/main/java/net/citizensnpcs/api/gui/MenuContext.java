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
    private String title;

    public MenuContext(InventoryMenu menu, InventoryMenuSlot[] slots, Inventory inventory, String title) {
        this(menu, slots, inventory, title, Collections.emptyMap());
    }

    public MenuContext(InventoryMenu menu, InventoryMenuSlot[] slots, Inventory inventory, String title,
            Map<String, Object> data) {
        this.inventory = inventory;
        this.title = title;
        this.slots = slots;
        this.menu = menu;
        this.data.putAll(data);
    }

    public void clearSlots() {
        for (int i = 0; i < slots.length; i++) {
            InventoryMenuSlot slot = slots[i];
            if (slot != null) {
                slot.clear();
            }
            slots[i] = null;
        }
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.menu.updateTitle(title);
    }
}
